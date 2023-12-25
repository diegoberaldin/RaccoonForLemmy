package com.github.diegoberaldin.raccoonforlemmy.unit.userdetail

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
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
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserDetailViewModel(
    private val mvi: DefaultMviModel<UserDetailMviModel.Intent, UserDetailMviModel.UiState, UserDetailMviModel.Effect>,
    private val user: UserModel,
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
) : UserDetailMviModel,
    MviModel<UserDetailMviModel.Intent, UserDetailMviModel.UiState, UserDetailMviModel.Effect> by mvi {

    private var currentPage = 1

    override fun onStarted() {
        mvi.onStarted()
        mvi.updateState {
            it.copy(
                instance = otherInstance.takeIf { n -> n.isNotEmpty() }
                    ?: apiConfigurationRepository.instance.value,
            )
        }
        mvi.scope?.launch {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    applySortType(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Share::class).onEach { evt ->
                shareHelper.share(evt.url)
            }.launchIn(this)
        }
        mvi.updateState {
            it.copy(
                user = it.user.takeIf { u -> u.id != 0 } ?: user,
            )
        }
        mvi.scope?.launch {
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        doubleTapActionEnabled = settings.enableDoubleTapAction,
                        sortType = settings.defaultPostSortType.toSortType(),
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        fullHeightImages = settings.fullHeightImages,
                    )
                }
            }.launchIn(this)
            identityRepository.isLogged.onEach { logged ->
                mvi.updateState { it.copy(isLogged = logged ?: false) }
            }.launchIn(this)
            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                mvi.updateState {
                    it.copy(
                        currentUserId = user?.id ?: 0,
                    )
                }
            }

            if (uiState.value.posts.isEmpty()) {
                updateAvailableSortTypes()
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: UserDetailMviModel.Intent) {
        when (intent) {
            is UserDetailMviModel.Intent.ChangeSort -> applySortType(intent.value)
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
            UserDetailMviModel.Intent.LoadNextPage -> mvi.scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }

            UserDetailMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
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
        }
    }

    private fun applySortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        mvi.scope?.launch {
            mvi.emitEffect(UserDetailMviModel.Effect.BackToTop)
        }
    }

    private fun changeSection(section: UserDetailSection) {
        mvi.updateState {
            it.copy(
                section = section,
            )
        }
        updateAvailableSortTypes()
    }

    private fun updateAvailableSortTypes() {
        mvi.scope?.launch(Dispatchers.IO) {
            val sortTypes = if (uiState.value.section == UserDetailSection.Posts) {
                getSortTypesUseCase.getTypesForPosts(otherInstance = otherInstance)
            } else {
                getSortTypesUseCase.getTypesForComments(otherInstance = otherInstance)
            }
            mvi.updateState { it.copy(availableSortTypes = sortTypes) }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage = 1
        mvi.updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
            )
        }
        val auth = identityRepository.authToken.value
        val refreshedUser = userRepository.get(
            id = user.id,
            auth = auth,
            otherInstance = otherInstance,
            username = user.name,
        )
        if (refreshedUser != null) {
            mvi.updateState { it.copy(user = refreshedUser) }
        }
        loadNextPage()
    }

    private suspend fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }
        mvi.updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val section = currentState.section
        val userId = currentState.user.id
        if (section == UserDetailSection.Posts) {
            val itemList = userRepository.getPosts(
                auth = auth,
                id = userId,
                page = currentPage,
                sort = currentState.sortType,
                username = user.name,
                otherInstance = otherInstance,
            )
            val comments = if (currentPage == 1 && currentState.comments.isEmpty()) {
                // this is needed because otherwise on first selector change
                // the lazy column scrolls back to top (it must have an empty data set)
                userRepository.getComments(
                    auth = auth,
                    id = userId,
                    page = currentPage,
                    sort = currentState.sortType,
                    username = user.name,
                    otherInstance = otherInstance,
                ).orEmpty()
            } else {
                currentState.comments
            }
            mvi.updateState {
                val newPosts = if (refreshing) {
                    itemList.orEmpty()
                } else {
                    it.posts + itemList.orEmpty()
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
                    comments = comments,
                    loading = false,
                    canFetchMore = itemList?.isEmpty() != true,
                    refreshing = false,
                    initial = false,
                )
            }
            if (!itemList.isNullOrEmpty()) {
                currentPage++
            }
        } else {
            val itemList = userRepository.getComments(
                auth = auth,
                id = userId,
                page = currentPage,
                sort = currentState.sortType,
                otherInstance = otherInstance,
            )

            mvi.updateState {
                val newcomments = if (refreshing) {
                    itemList.orEmpty()
                } else {
                    it.comments + itemList.orEmpty()
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
                currentPage++
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.updateState {
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
        mvi.updateState {
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
        mvi.updateState { it.copy(asyncInProgress = true) }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val userId = user.id
                val auth = identityRepository.authToken.value
                userRepository.block(userId, true, auth).getOrThrow()
                mvi.emitEffect(UserDetailMviModel.Effect.BlockSuccess)
            } catch (e: Throwable) {
                mvi.emitEffect(UserDetailMviModel.Effect.BlockError(e.message))
            } finally {
                mvi.updateState { it.copy(asyncInProgress = false) }
            }
        }
    }

    private fun blockInstance() {
        mvi.updateState { it.copy(asyncInProgress = true) }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val instanceId = user.instanceId
                val auth = identityRepository.authToken.value
                siteRepository.block(instanceId, true, auth).getOrThrow()
                mvi.emitEffect(UserDetailMviModel.Effect.BlockSuccess)
            } catch (e: Throwable) {
                mvi.emitEffect(UserDetailMviModel.Effect.BlockError(e.message))
            } finally {
                mvi.updateState { it.copy(asyncInProgress = false) }
            }
        }
    }
}
