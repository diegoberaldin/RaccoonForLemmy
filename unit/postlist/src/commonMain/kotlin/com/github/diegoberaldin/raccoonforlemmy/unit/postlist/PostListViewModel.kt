package com.github.diegoberaldin.raccoonforlemmy.unit.postlist

import cafe.adriel.voyager.core.model.screenModelScope
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.PostPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.PostPaginationSpecification
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.ImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.zombiemode.ZombieModeHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostListViewModel(
    private val postPaginationManager: PostPaginationManager,
    private val postRepository: PostRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val shareHelper: ShareHelper,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
    private val zombieModeHelper: ZombieModeHelper,
    private val imagePreloadManager: ImagePreloadManager,
    private val getSortTypesUseCase: GetSortTypesUseCase,
) : PostListMviModel,
    DefaultMviModel<PostListMviModel.Intent, PostListMviModel.UiState, PostListMviModel.Effect>(
        initialState = PostListMviModel.UiState()
    ) {

    private var hideReadPosts = false

    init {
        screenModelScope.launch {
            apiConfigurationRepository.instance.onEach { instance ->
                updateState {
                    it.copy(instance = instance)
                }
            }.launchIn(this)

            identityRepository.isLogged.onEach { logged ->
                updateState {
                    it.copy(isLogged = logged ?: false)
                }
                updateAvailableSortTypes()
            }.launchIn(this)

            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)

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
                        showScores = settings.showScores,
                        fadeReadPosts = settings.fadeReadPosts,
                        showUnreadComments = settings.showUnreadComments,
                    )
                }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class)
                .onEach { evt ->
                    handlePostUpdate(evt.model)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostDeleted::class)
                .onEach { evt ->
                    handlePostDelete(evt.model.id)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeFeedType::class)
                .onEach { evt ->
                    if (evt.screenKey == "postList") {
                        applyListingType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    if (evt.screenKey == "postList") {
                        applySortType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                handleLogout()
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.InstanceSelected::class).onEach {
                refresh(initial = true)
                delay(100)
                emitEffect(PostListMviModel.Effect.BackToTop)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.BlockActionSelected::class)
                .onEach { evt ->
                    val userId = evt.userId
                    val communityId = evt.communityId
                    val instanceId = evt.instanceId
                    when {
                        userId != null -> blockUser(userId)
                        communityId != null -> blockCommunity(communityId)
                        instanceId != null -> blockInstance(instanceId)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Share::class).onEach { evt ->
                shareHelper.share(evt.url)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ResetHome::class).onEach {
                onFirstLoad()
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CopyText::class).onEach {
                emitEffect(PostListMviModel.Effect.TriggerCopy(it.value))
            }.launchIn(this)

            zombieModeHelper.index.onEach { index ->
                if (uiState.value.zombieModeActive) {
                    emitEffect(PostListMviModel.Effect.ZombieModeTick(index))
                }
            }.launchIn(this)

            val auth = identityRepository.authToken.value.orEmpty()
            val user = siteRepository.getCurrentUser(auth)
            updateState { it.copy(currentUserId = user?.id ?: 0) }
        }

        onFirstLoad()
    }

    private fun onFirstLoad() {
        val settings = settingsRepository.currentSettings.value
        updateState {
            it.copy(
                listingType = settings.defaultListingType.toListingType(),
                sortType = settings.defaultPostSortType.toSortType(),
            )
        }
        screenModelScope.launch {
            refresh(initial = true)
            emitEffect(PostListMviModel.Effect.BackToTop)
        }
    }

    private suspend fun updateAvailableSortTypes() {
        val sortTypes = getSortTypesUseCase.getTypesForPosts()
        updateState { it.copy(availableSortTypes = sortTypes) }
    }

    override fun reduce(intent: PostListMviModel.Intent) {
        when (intent) {
            PostListMviModel.Intent.LoadNextPage -> screenModelScope.launch {
                loadNextPage()
            }

            PostListMviModel.Intent.Refresh -> screenModelScope.launch {
                refresh()
            }

            is PostListMviModel.Intent.ChangeListing -> applyListingType(intent.value)
            is PostListMviModel.Intent.DownVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleDownVote(post = post)
                }
            }

            is PostListMviModel.Intent.SavePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleSave(post = post)
                }
            }

            is PostListMviModel.Intent.UpVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleUpVote(post = post)
                }
            }

            PostListMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is PostListMviModel.Intent.HandlePostUpdate -> handlePostUpdate(intent.post)
            is PostListMviModel.Intent.DeletePost -> handlePostDelete(intent.id)
            is PostListMviModel.Intent.Share -> {
                shareHelper.share(intent.url)
            }

            is PostListMviModel.Intent.MarkAsRead -> {
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    markAsRead(post = post)
                }
            }

            PostListMviModel.Intent.ClearRead -> clearRead()
            is PostListMviModel.Intent.Hide -> {
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    hide(post = post)
                }
            }

            PostListMviModel.Intent.PauseZombieMode -> {
                updateState { it.copy(zombieModeActive = false) }
                zombieModeHelper.pause()
            }

            is PostListMviModel.Intent.StartZombieMode -> {
                updateState { it.copy(zombieModeActive = true) }
                zombieModeHelper.start(
                    initialValue = intent.index,
                    interval = settingsRepository.currentSettings.value.zombieModeInterval,
                )
            }

            is PostListMviModel.Intent.Copy -> screenModelScope.launch {
                emitEffect(PostListMviModel.Effect.TriggerCopy(intent.value))
            }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        hideReadPosts = false
        val listingType = uiState.value.listingType ?: return
        val sortType = uiState.value.sortType ?: return
        postPaginationManager.reset(
            PostPaginationSpecification.Listing(
                listingType = listingType,
                sortType = sortType,
                includeNsfw = settingsRepository.currentSettings.value.includeNsfw,
            )
        )
        updateState {
            it.copy(
                initial = initial,
                canFetchMore = true,
                refreshing = true,
                loading = false,
            )
        }
        loadNextPage()
        if (identityRepository.isLogged.value == null) {
            identityRepository.refreshLoggedState()
        }
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }
        updateState { it.copy(loading = true) }
        val refreshing = currentState.refreshing
        val posts = postPaginationManager.loadNextPage().let {
            if (!hideReadPosts) {
                it
            } else {
                it.filter { post -> !post.read }
            }
        }
        if (uiState.value.autoLoadImages) {
            posts.forEach { post ->
                post.imageUrl.takeIf { i -> i.isNotEmpty() }?.also { url ->
                    imagePreloadManager.preload(url)
                }
            }
        }
        updateState {
            it.copy(
                posts = posts,
                loading = if (it.initial) posts.isEmpty() else false,
                canFetchMore = postPaginationManager.canFetchMore,
                refreshing = false,
                initial = if (it.initial) posts.isEmpty() else false,
            )
        }
    }

    private fun applySortType(value: SortType) {
        if (uiState.value.sortType == value) {
            return
        }
        updateState { it.copy(sortType = value) }
        screenModelScope.launch {
            emitEffect(PostListMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private fun applyListingType(value: ListingType) {
        if (uiState.value.listingType == value) {
            return
        }
        updateState { it.copy(listingType = value) }
        screenModelScope.launch(Dispatchers.IO) {
            emitEffect(PostListMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private fun toggleUpVote(post: PostModel) {
        val newVote = post.myVote <= 0
        val newPost = postRepository.asUpVoted(
            post = post,
            voted = newVote,
        )
        handlePostUpdate(newPost)
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.upVote(
                    post = post,
                    auth = auth,
                    voted = newVote,
                )
                markAsRead(newPost)
            } catch (e: Throwable) {
                e.printStackTrace()
                handlePostUpdate(post)
            }
        }
    }

    private fun markAsRead(post: PostModel) {
        if (post.read) {
            return
        }
        val newPost = post.copy(read = true)
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.setRead(
                    read = true,
                    postId = post.id,
                    auth = auth,
                )
                handlePostUpdate(newPost)
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
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.downVote(
                    post = post,
                    auth = auth,
                    downVoted = newValue,
                )
                markAsRead(newPost)
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
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.save(
                    post = post,
                    auth = auth,
                    saved = newValue,
                )
                markAsRead(newPost)
            } catch (e: Throwable) {
                e.printStackTrace()
                handlePostUpdate(post)
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

    private fun handleLogout() {
        updateState {
            it.copy(
                posts = emptyList(),
                isLogged = false,
            )
        }
        onFirstLoad()
    }

    private fun handlePostDelete(id: Long) {
        screenModelScope.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            postRepository.delete(id = id, auth = auth)
            handlePostDelete(id)
        }
    }

    private fun clearRead() {
        hideReadPosts = true
        updateState {
            val newPosts = it.posts.filter { e -> !e.read }
            it.copy(posts = newPosts)
        }
    }

    private fun hide(post: PostModel) {
        updateState {
            val newPosts = it.posts.filter { e -> e.id != post.id }
            it.copy(
                posts = newPosts,
            )
        }
        markAsRead(post)
    }

    private fun blockUser(userId: Long) {
        screenModelScope.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            userRepository.block(userId, true, auth)
        }
    }

    private fun blockCommunity(communityId: Long) {
        screenModelScope.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            communityRepository.block(communityId, true, auth)
        }
    }

    private fun blockInstance(instanceId: Long) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value
                siteRepository.block(instanceId, true, auth)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
