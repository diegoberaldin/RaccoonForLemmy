package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.detail

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.ImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.utils.MultiCommunityPaginator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MultiCommunityViewModel(
    private val communityId: Long,
    private val postRepository: PostRepository,
    private val identityRepository: IdentityRepository,
    private val multiCommunityRepository: MultiCommunityRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val shareHelper: ShareHelper,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
    private val paginator: MultiCommunityPaginator,
    private val imagePreloadManager: ImagePreloadManager,
    private val getSortTypesUseCase: GetSortTypesUseCase,
) : MultiCommunityMviModel,
    DefaultMviModel<MultiCommunityMviModel.Intent, MultiCommunityMviModel.UiState, MultiCommunityMviModel.Effect>(
        initialState = MultiCommunityMviModel.UiState()
    ) {

    private var hideReadPosts = false

    init {
        screenModelScope.launch {
            if ((uiState.value.community.id ?: 0) == 0L) {
                val community =
                    multiCommunityRepository.getById(communityId) ?: MultiCommunityModel()
                updateState { it.copy(community = community) }
            }
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)

            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        fullHeightImages = settings.fullHeightImages,
                        actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts,
                        actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts,
                        showScores = settings.showScores,
                        fadeReadPosts = settings.fadeReadPosts,
                    )
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    if (evt.screenKey == "multiCommunity") {
                        applySortType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Share::class).onEach { evt ->
                shareHelper.share(evt.url)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CopyText::class).onEach {
                emitEffect(MultiCommunityMviModel.Effect.TriggerCopy(it.value))
            }.launchIn(this)
            identityRepository.isLogged.onEach { logged ->
                updateState { it.copy(isLogged = logged ?: false) }
            }.launchIn(this)

            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                updateState { it.copy(currentUserId = user?.id ?: 0) }
            }
            withContext(Dispatchers.IO) {
                if (uiState.value.posts.isEmpty()) {
                    val settings = settingsRepository.currentSettings.value
                    val sortTypes = getSortTypesUseCase.getTypesForPosts()
                    updateState {
                        it.copy(
                            sortType = settings.defaultPostSortType.toSortType(),
                            availableSortTypes = sortTypes,
                        )
                    }
                    paginator.setCommunities(uiState.value.community.communityIds)
                    refresh(initial = true)
                }
            }
        }
    }

    override fun reduce(intent: MultiCommunityMviModel.Intent) {
        when (intent) {
            is MultiCommunityMviModel.Intent.DownVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleDownVote(
                    post = uiState.value.posts.first { it.id == intent.id },
                )
            }

            MultiCommunityMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            MultiCommunityMviModel.Intent.LoadNextPage -> loadNextPage()
            MultiCommunityMviModel.Intent.Refresh -> refresh()
            is MultiCommunityMviModel.Intent.SavePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleSave(
                    post = uiState.value.posts.first { it.id == intent.id },
                )
            }

            is MultiCommunityMviModel.Intent.Share -> {
                shareHelper.share(intent.url)
            }

            is MultiCommunityMviModel.Intent.UpVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleUpVote(post = uiState.value.posts.first { it.id == intent.id })
            }

            MultiCommunityMviModel.Intent.ClearRead -> clearRead()
            is MultiCommunityMviModel.Intent.MarkAsRead -> markAsRead(
                post = uiState.value.posts.first { it.id == intent.id },
            )

            is MultiCommunityMviModel.Intent.Hide -> hide(
                post = uiState.value.posts.first { it.id == intent.id },
            )

            is MultiCommunityMviModel.Intent.Copy -> screenModelScope.launch {
                emitEffect(MultiCommunityMviModel.Effect.TriggerCopy(intent.value))
            }
        }
    }

    private fun refresh(initial: Boolean = false) {
        hideReadPosts = false
        paginator.reset()
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
                loading = false,
            )
        }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }

        screenModelScope.launch(Dispatchers.IO) {
            updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val sort = currentState.sortType ?: SortType.Active
            val refreshing = currentState.refreshing
            val includeNsfw = settingsRepository.currentSettings.value.includeNsfw

            val itemList = paginator.loadNextPage(
                auth = auth,
                sort = sort,
                currentIds = if (refreshing) emptyList() else currentState.posts.map { it.id }
            )
            val canFetchMore = paginator.canFetchMore
            val itemsToAdd = itemList.let {
                if (includeNsfw) {
                    it
                } else {
                    it.filter { post -> !post.nsfw }
                }
            }.let {
                if (!hideReadPosts) {
                    it
                } else {
                    it.filter { post -> !post.read }
                }
            }
            if (uiState.value.autoLoadImages) {
                itemsToAdd.forEach { post ->
                    post.imageUrl.takeIf { i -> i.isNotEmpty() }?.also { url ->
                        imagePreloadManager.preload(url)
                    }
                }
            }
            updateState {
                val newPosts = if (refreshing) {
                    itemsToAdd
                } else {
                    it.posts + itemsToAdd
                }
                it.copy(
                    posts = newPosts,
                    loading = false,
                    canFetchMore = canFetchMore,
                    refreshing = false,
                    initial = newPosts.isEmpty(),
                )
            }
        }
    }

    private fun applySortType(value: SortType) {
        if (uiState.value.sortType == value) {
            return
        }
        updateState { it.copy(sortType = value) }
        refresh()
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
}
