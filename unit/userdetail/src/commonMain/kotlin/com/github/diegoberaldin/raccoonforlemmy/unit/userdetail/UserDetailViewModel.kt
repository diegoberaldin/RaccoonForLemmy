package com.github.diegoberaldin.raccoonforlemmy.unit.userdetail

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.UserDetailSection
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.ImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserDetailViewModel(
    private val userId: Int,
    private val otherInstance: String = "",
    private val identityRepository: IdentityRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val shareHelper: ShareHelper,
    private val hapticFeedback: HapticFeedback,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val imagePreloadManager: ImagePreloadManager,
    private val getSortTypesUseCase: GetSortTypesUseCase,
    private val itemCache: LemmyItemCache,
) : UserDetailMviModel,
    DefaultMviModel<UserDetailMviModel.Intent, UserDetailMviModel.UiState, UserDetailMviModel.Effect>(
        initialState = UserDetailMviModel.UiState(),
    ) {

    private val currentPage = mutableMapOf<UserDetailSection, Int>()

    init {
        updateState {
            it.copy(
                instance = otherInstance.takeIf { n -> n.isNotEmpty() }
                    ?: apiConfigurationRepository.instance.value,
            )
        }
        screenModelScope.launch {
            if (uiState.value.user.id == 0) {
                val user = itemCache.getUser(userId) ?: UserModel()
                updateState {
                    it.copy(user = user)
                }
            }
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    if (evt.screenKey == uiState.value.user.readableHandle) {
                        applySortType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Share::class).onEach { evt ->
                shareHelper.share(evt.url)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CopyText::class).onEach {
                emitEffect(UserDetailMviModel.Effect.TriggerCopy(it.value))
            }.launchIn(this)
        }

        screenModelScope.launch {
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        doubleTapActionEnabled = settings.enableDoubleTapAction,
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        fullHeightImages = settings.fullHeightImages,
                        actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts,
                        actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts,
                        actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments,
                        actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments,
                        showScores = settings.showScores,
                    )
                }
            }.launchIn(this)
            identityRepository.isLogged.onEach { logged ->
                updateState { it.copy(isLogged = logged ?: false) }
                updateAvailableSortTypes()
            }.launchIn(this)
            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                updateState {
                    it.copy(
                        currentUserId = user?.id ?: 0,
                    )
                }
            }

            if (uiState.value.posts.isEmpty()) {
                val defaultPostSortType =
                    settingsRepository.currentSettings.value.defaultPostSortType
                updateState { it.copy(sortType = defaultPostSortType.toSortType()) }

                withContext(Dispatchers.IO) {
                    refresh(initial = true)
                }
            }
        }
    }

    override fun reduce(intent: UserDetailMviModel.Intent) {
        when (intent) {
            is UserDetailMviModel.Intent.ChangeSection -> changeSection(intent.section)
            is UserDetailMviModel.Intent.DownVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.id }?.also { comment ->
                    toggleDownVoteComment(comment = comment)
                }
            }

            is UserDetailMviModel.Intent.DownVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleDownVote(
                        post = post,
                    )
                }
            }

            UserDetailMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            UserDetailMviModel.Intent.LoadNextPage -> screenModelScope.launch {
                loadNextPage()
            }

            UserDetailMviModel.Intent.Refresh -> screenModelScope.launch {
                refresh()
            }

            is UserDetailMviModel.Intent.SaveComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.id }?.also { comment ->
                    toggleSaveComment(comment = comment)
                }
            }

            is UserDetailMviModel.Intent.SavePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleSave(post = post)
                }
            }

            is UserDetailMviModel.Intent.UpVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.id }?.also { comment ->
                    toggleUpVoteComment(comment = comment)
                }
            }

            is UserDetailMviModel.Intent.UpVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleUpVote(post = post)
                }
            }

            is UserDetailMviModel.Intent.Share -> {
                shareHelper.share(intent.url)
            }

            UserDetailMviModel.Intent.Block -> blockUser()
            UserDetailMviModel.Intent.BlockInstance -> blockInstance()
            is UserDetailMviModel.Intent.Copy -> screenModelScope.launch {
                emitEffect(UserDetailMviModel.Effect.TriggerCopy(intent.value))
            }
        }
    }

    private fun applySortType(value: SortType) {
        if (uiState.value.sortType == value) {
            return
        }
        updateState { it.copy(sortType = value) }
        screenModelScope.launch(Dispatchers.Main) {
            emitEffect(UserDetailMviModel.Effect.BackToTop)
        }
    }

    private fun changeSection(section: UserDetailSection) {
        updateState {
            it.copy(
                section = section,
            )
        }
    }

    private fun updateAvailableSortTypes() {
        screenModelScope.launch {
            val sortTypes = if (uiState.value.section == UserDetailSection.Posts) {
                getSortTypesUseCase.getTypesForPosts(otherInstance = otherInstance)
            } else {
                getSortTypesUseCase.getTypesForComments(otherInstance = otherInstance)
            }
            updateState { it.copy(availableSortTypes = sortTypes) }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage[UserDetailSection.Posts] = 1
        currentPage[UserDetailSection.Comments] = 1
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
            )
        }
        val auth = identityRepository.authToken.value
        val refreshedUser = userRepository.get(
            id = userId,
            auth = auth,
            otherInstance = otherInstance,
            username = uiState.value.user.name,
        )
        if (refreshedUser != null) {
            updateState { it.copy(user = refreshedUser) }
        }
        loadNextPage()
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }
        updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val section = currentState.section
        val userId = currentState.user.id
        val includeNsfw = settingsRepository.currentSettings.value.includeNsfw
        if (section == UserDetailSection.Posts) {
            val page = currentPage[UserDetailSection.Posts] ?: 1
            coroutineScope {
                val itemList = async {
                    userRepository.getPosts(
                        auth = auth,
                        id = userId,
                        page = page,
                        sort = currentState.sortType,
                        username = uiState.value.user.name,
                        otherInstance = otherInstance,
                    )
                }.await()
                val comments = async {
                    if (page == 1 && (currentState.comments.isEmpty() || refreshing)) {
                        // this is needed because otherwise on first selector change
                        // the lazy column scrolls back to top (it must have an empty data set)
                        userRepository.getComments(
                            auth = auth,
                            id = userId,
                            page = 1,
                            sort = currentState.sortType,
                            username = uiState.value.user.name,
                            otherInstance = otherInstance,
                        ).orEmpty()
                    } else {
                        currentState.comments
                    }
                }.await()
                val postsToAdd = itemList.orEmpty()
                    .let {
                        if (includeNsfw) {
                            it
                        } else {
                            it.filter { post -> !post.nsfw }
                        }
                    }
                    .filterNot { post ->
                        post.deleted
                    }
                val commentsToAdd = comments
                    .filterNot { post ->
                        post.deleted
                    }
                updateState {
                    val newPosts = if (refreshing) {
                        postsToAdd
                    } else {
                        it.posts + postsToAdd
                    }
                    if (uiState.value.autoLoadImages) {
                        newPosts.forEach { post ->
                            post.imageUrl.takeIf { i -> i.isNotEmpty() }?.also { url ->
                                imagePreloadManager.preload(url)
                            }
                        }
                    }
                    it.copy(
                        posts = newPosts,
                        comments = commentsToAdd,
                        loading = false,
                        canFetchMore = itemList?.isEmpty() != true,
                        refreshing = false,
                        initial = false,
                    )
                }
                if (!itemList.isNullOrEmpty()) {
                    currentPage[UserDetailSection.Posts] = page + 1
                }
            }
        } else {
            val page = currentPage[UserDetailSection.Comments] ?: 1
            val itemList = userRepository.getComments(
                auth = auth,
                id = userId,
                page = page,
                sort = currentState.sortType,
                otherInstance = otherInstance,
            )
            val commentsToAdd = itemList.orEmpty()
                .filterNot { post ->
                    post.deleted
                }
            updateState {
                val newcomments = if (refreshing) {
                    commentsToAdd
                } else {
                    it.comments + commentsToAdd
                }
                it.copy(
                    comments = newcomments,
                    loading = false,
                    canFetchMore = itemList?.isEmpty() != true,
                    refreshing = false,
                    initial = false,
                )
            }
            if (!itemList.isNullOrEmpty()) {
                currentPage[UserDetailSection.Comments] = page + 1
            }
        }
    }

    private fun toggleUpVote(post: PostModel) {
        val newVote = post.myVote <= 0
        val newPost = postRepository.asUpVoted(
            post = post,
            voted = newVote,
        )
        handlePostUpdate(newPost)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.upVote(
                    post = post,
                    auth = auth,
                    voted = newVote,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handlePostUpdate(post)
            }
        }
    }

    private fun toggleDownVote(post: PostModel) {
        val newValue = post.myVote >= 0
        val newPost = postRepository.asDownVoted(
            post = post,
            downVoted = newValue,
        )
        handlePostUpdate(newPost)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.downVote(
                    post = post,
                    auth = auth,
                    downVoted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handlePostUpdate(post)
            }
        }
    }

    private fun toggleSave(post: PostModel) {
        val newValue = !post.saved
        val newPost = postRepository.asSaved(
            post = post,
            saved = newValue,
        )
        handlePostUpdate(newPost)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.save(
                    post = post,
                    auth = auth,
                    saved = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handlePostUpdate(post)
            }
        }
    }

    private fun toggleUpVoteComment(comment: CommentModel) {
        val newValue = comment.myVote <= 0
        val newComment = commentRepository.asUpVoted(
            comment = comment,
            voted = newValue,
        )
        handleCommentUpdate(newComment)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.upVote(
                    auth = auth,
                    comment = comment,
                    voted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
            }
        }
    }

    private fun toggleDownVoteComment(comment: CommentModel) {
        val newValue = comment.myVote >= 0
        val newComment = commentRepository.asDownVoted(comment, newValue)
        handleCommentUpdate(newComment)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.downVote(
                    auth = auth,
                    comment = comment,
                    downVoted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
            }
        }
    }

    private fun toggleSaveComment(comment: CommentModel) {
        val newValue = !comment.saved
        val newComment = commentRepository.asSaved(
            comment = comment,
            saved = newValue,
        )
        handleCommentUpdate(newComment)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.save(
                    auth = auth,
                    comment = comment,
                    saved = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
            }
        }
    }

    private fun handlePostUpdate(post: PostModel) {
        updateState {
            it.copy(
                posts = it.posts.map { p ->
                    if (p.id == post.id) {
                        post
                    } else {
                        p
                    }
                },
            )
        }
    }

    private fun handleCommentUpdate(comment: CommentModel) {
        updateState {
            it.copy(
                comments = it.comments.map { c ->
                    if (c.id == comment.id) {
                        comment
                    } else {
                        c
                    }
                },
            )
        }
    }

    private fun blockUser() {
        updateState { it.copy(asyncInProgress = true) }
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value
                userRepository.block(userId, true, auth).getOrThrow()
                emitEffect(UserDetailMviModel.Effect.BlockSuccess)
            } catch (e: Throwable) {
                emitEffect(UserDetailMviModel.Effect.BlockError(e.message))
            } finally {
                updateState { it.copy(asyncInProgress = false) }
            }
        }
    }

    private fun blockInstance() {
        updateState { it.copy(asyncInProgress = true) }
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val user = uiState.value.user
                val instanceId = user.instanceId
                val auth = identityRepository.authToken.value
                siteRepository.block(instanceId, true, auth).getOrThrow()
                emitEffect(UserDetailMviModel.Effect.BlockSuccess)
            } catch (e: Throwable) {
                emitEffect(UserDetailMviModel.Effect.BlockError(e.message))
            } finally {
                updateState { it.copy(asyncInProgress = false) }
            }
        }
    }
}
