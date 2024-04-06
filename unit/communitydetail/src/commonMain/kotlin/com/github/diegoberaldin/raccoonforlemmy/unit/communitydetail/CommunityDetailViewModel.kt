package com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.FavoriteCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.CommunitySortRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.FavoriteCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.ImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.zombiemode.ZombieModeHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.containsId
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CommunityDetailViewModel(
    private val communityId: Long,
    private val otherInstance: String,
    private val identityRepository: IdentityRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val communityRepository: CommunityRepository,
    private val postRepository: PostRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val favoriteCommunityRepository: FavoriteCommunityRepository,
    private val shareHelper: ShareHelper,
    private val hapticFeedback: HapticFeedback,
    private val zombieModeHelper: ZombieModeHelper,
    private val imagePreloadManager: ImagePreloadManager,
    private val getSortTypesUseCase: GetSortTypesUseCase,
    private val notificationCenter: NotificationCenter,
    private val itemCache: LemmyItemCache,
    private val communitySortRepository: CommunitySortRepository,
) : CommunityDetailMviModel,
    DefaultMviModel<CommunityDetailMviModel.Intent, CommunityDetailMviModel.UiState, CommunityDetailMviModel.Effect>(
        initialState = CommunityDetailMviModel.UiState(),
    ) {

    private var currentPage: Int = 1
    private var pageCursor: String? = null
    private var hideReadPosts = false
    private val searchEventChannel = Channel<Unit>()

    init {
        screenModelScope.launch {
            if (uiState.value.community.id == 0L) {
                val community = itemCache.getCommunity(communityId) ?: CommunityModel()
                updateState {
                    it.copy(
                        community = community,
                        instance = otherInstance.takeIf { n -> n.isNotEmpty() }
                            ?: apiConfigurationRepository.instance.value,
                    )
                }
            }

            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            identityRepository.isLogged.onEach { logged ->
                updateState { it.copy(isLogged = logged ?: false) }
                updateAvailableSortTypes()
            }.launchIn(this)

            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        doubleTapActionEnabled = settings.enableDoubleTapAction,
                        fullHeightImages = settings.fullHeightImages,
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts,
                        actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts,
                        showScores = settings.showScores,
                    )
                }
            }.launchIn(this)

            zombieModeHelper.index.onEach { index ->
                if (uiState.value.zombieModeActive) {
                    emitEffect(CommunityDetailMviModel.Effect.ZombieModeTick(index))
                }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostRemoved::class).onEach { evt ->
                handlePostDelete(evt.model.id)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.UserBannedPost::class)
                .onEach { evt ->
                    val postId = evt.postId
                    val newUser = evt.user
                    updateState {
                        it.copy(
                            posts = it.posts.map { p ->
                                if (p.id == postId) {
                                    p.copy(
                                        creator = newUser,
                                        updateDate = newUser.updateDate,
                                    )
                                } else {
                                    p
                                }
                            },
                        )
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CommentRemoved::class)
                .onEach { evt ->
                    val postId = evt.model.postId
                    uiState.value.posts.firstOrNull { it.id == postId }?.also {
                        val newPost = it.copy(comments = (it.comments - 1).coerceAtLeast(0))
                        handlePostUpdate(newPost)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    if (evt.screenKey == uiState.value.community.readableHandle) {
                        if (evt.defaultForCommunity) {
                            val handle = uiState.value.community.readableHandle
                            communitySortRepository.saveSort(handle, evt.value.toInt())
                        }
                        applySortType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Share::class).onEach { evt ->
                shareHelper.share(evt.url)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CopyText::class).onEach {
                emitEffect(CommunityDetailMviModel.Effect.TriggerCopy(it.value))
            }.launchIn(this)

            searchEventChannel.receiveAsFlow().debounce(1_000).onEach {
                updateState { it.copy(loading = false) }
                emitEffect(CommunityDetailMviModel.Effect.BackToTop)
                refresh()
            }.launchIn(this)

            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                updateState { it.copy(currentUserId = user?.id ?: 0) }
            }
            if (uiState.value.posts.isEmpty()) {
                val defaultPostSortType = settingsRepository.currentSettings.value.defaultPostSortType.toSortType()
                val customPostSortType =
                    communitySortRepository.getSort(uiState.value.community.readableHandle)?.toSortType()
                updateState { it.copy(sortType = customPostSortType ?: defaultPostSortType) }
                refresh()
            }
        }
    }

    private suspend fun updateAvailableSortTypes() {
        val sortTypes = getSortTypesUseCase.getTypesForPosts(otherInstance = otherInstance)
        updateState { it.copy(availableSortTypes = sortTypes) }
    }

    override fun reduce(intent: CommunityDetailMviModel.Intent) {
        when (intent) {
            CommunityDetailMviModel.Intent.LoadNextPage -> screenModelScope.launch(Dispatchers.IO) {
                loadNextPage()
            }

            CommunityDetailMviModel.Intent.Refresh -> screenModelScope.launch(Dispatchers.IO) {
                refresh()
            }

            is CommunityDetailMviModel.Intent.DownVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleDownVotePost(post = post)
                }
            }

            is CommunityDetailMviModel.Intent.Share -> {
                shareHelper.share(intent.url)
            }

            is CommunityDetailMviModel.Intent.SavePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleSavePost(post = post)
                }
            }

            is CommunityDetailMviModel.Intent.UpVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleUpVotePost(post = post)
                }
            }

            CommunityDetailMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            CommunityDetailMviModel.Intent.Subscribe -> subscribe()
            CommunityDetailMviModel.Intent.Unsubscribe -> unsubscribe()
            is CommunityDetailMviModel.Intent.DeletePost -> handlePostDelete(intent.id)

            CommunityDetailMviModel.Intent.Block -> blockCommunity()
            CommunityDetailMviModel.Intent.BlockInstance -> blockInstance()
            is CommunityDetailMviModel.Intent.MarkAsRead -> {
                markAsRead(uiState.value.posts.first { it.id == intent.id })
            }

            CommunityDetailMviModel.Intent.ClearRead -> clearRead()
            is CommunityDetailMviModel.Intent.Hide -> {
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    hide(post = post)
                }
            }

            CommunityDetailMviModel.Intent.PauseZombieMode -> {
                updateState { it.copy(zombieModeActive = false) }
                zombieModeHelper.pause()
            }

            is CommunityDetailMviModel.Intent.StartZombieMode -> {
                updateState { it.copy(zombieModeActive = true) }
                zombieModeHelper.start(
                    initialValue = intent.index,
                    interval = settingsRepository.currentSettings.value.zombieModeInterval,
                )
            }

            is CommunityDetailMviModel.Intent.ModFeaturePost -> uiState.value.posts.firstOrNull { it.id == intent.id }
                ?.also { post ->
                    feature(post = post)
                }

            is CommunityDetailMviModel.Intent.ModLockPost -> uiState.value.posts.firstOrNull { it.id == intent.id }
                ?.also { post ->
                    lock(post = post)
                }

            is CommunityDetailMviModel.Intent.ModToggleModUser -> {
                toggleModeratorStatus(intent.id)
            }

            CommunityDetailMviModel.Intent.ToggleFavorite -> {
                toggleFavorite()
            }

            is CommunityDetailMviModel.Intent.ChangeSearching -> {
                updateState { it.copy(searching = intent.value) }
                if (!intent.value) {
                    updateSearchText("")
                }
            }

            is CommunityDetailMviModel.Intent.SetSearch -> updateSearchText(intent.value)
            is CommunityDetailMviModel.Intent.Copy -> screenModelScope.launch {
                emitEffect(CommunityDetailMviModel.Effect.TriggerCopy(intent.value))
            }
        }
    }

    private suspend fun refresh() {
        currentPage = 1
        pageCursor = null
        hideReadPosts = false
        updateState { it.copy(canFetchMore = true, refreshing = true) }
        val community = uiState.value.community
        val auth = identityRepository.authToken.value
        val accountId = accountRepository.getActive()?.id
        val isFavorite = favoriteCommunityRepository.getBy(accountId, community.id) != null
        val refreshedCommunity = communityRepository.get(
            auth = auth,
            name = community.name,
            id = community.id,
            instance = otherInstance,
        )?.copy(favorite = isFavorite)
        val moderators = communityRepository.getModerators(
            auth = auth,
            id = community.id,
        )
        if (refreshedCommunity != null) {
            updateState {
                it.copy(
                    community = refreshedCommunity,
                    moderators = moderators,
                    loading = false,
                )
            }
        }

        loadNextPage()
    }

    private fun applySortType(value: SortType) {
        if (uiState.value.sortType == value) {
            return
        }
        updateState { it.copy(sortType = value) }
        screenModelScope.launch(Dispatchers.IO) {
            emitEffect(CommunityDetailMviModel.Effect.BackToTop)
            refresh()
        }
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
        val sort = currentState.sortType
        val community = currentState.community
        val includeNsfw = settingsRepository.currentSettings.value.includeNsfw

        val (itemList, nextPage) = if (currentState.searching) {
            communityRepository.search(
                auth = auth,
                communityId = community.id,
                page = currentPage,
                sortType = sort,
                resultType = SearchResultType.Posts,
                query = currentState.searchText,
            ).mapNotNull {
                (it as? SearchResult.Post)?.model
            }.let { posts ->
                if (refreshing) {
                    posts
                } else {
                    // prevents accidental duplication
                    posts.filter { p1 ->
                        currentState.posts.none { p2 -> p2.id == p1.id }
                    }
                }
            } to null
        } else {
            postRepository.getAll(
                auth = auth,
                otherInstance = otherInstance,
                communityId = community.id,
                communityName = community.name,
                page = currentPage,
                pageCursor = pageCursor,
                sort = sort,
            )?.let {
                if (refreshing) {
                    it
                } else {
                    // prevents accidental duplication
                    val posts = it.first
                    it.copy(
                        first = posts.filter { p1 ->
                            currentState.posts.none { p2 -> p2.id == p1.id }
                        },
                    )
                }
            } ?: (null to null)
        }

        if (!itemList.isNullOrEmpty()) {
            currentPage++
        }
        if (nextPage != null) {
            pageCursor = nextPage
        }
        val itemsToAdd = itemList.orEmpty()
            .filterNot { post ->
                post.deleted
            }.let {
                if (!hideReadPosts) {
                    it
                } else {
                    it.filter { post -> !post.read }
                }
            }.let {
                if (includeNsfw || community.nsfw) {
                    it
                } else {
                    it.filter { post -> !post.nsfw }
                }
            }.let {
                if (currentState.searching) {
                    it.filter { post ->
                        listOf(post.title, post.text).any { s ->
                            s.contains(
                                other = currentState.searchText,
                                ignoreCase = true,
                            )
                        }
                    }
                } else {
                    it
                }
            }
        if (uiState.value.autoLoadImages) {
            itemsToAdd.forEach { post ->
                post.imageUrl.takeIf { i -> i.isNotEmpty() }?.also { url ->
                    imagePreloadManager.preload(url)
                }
            }
        }
        val newItems = if (refreshing) {
            itemsToAdd
        } else {
            currentState.posts + itemsToAdd
        }
        updateState {
            it.copy(
                posts = newItems,
                loading = false,
                canFetchMore = itemList?.isEmpty() != true,
                refreshing = false,
            )
        }
    }

    private fun toggleUpVotePost(post: PostModel) {
        val newValue = post.myVote <= 0
        val newPost = postRepository.asUpVoted(
            post = post,
            voted = newValue,
        )
        handlePostUpdate(newPost)
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.upVote(
                    auth = auth,
                    post = post,
                    voted = newValue,
                )
                markAsRead(newPost)
                notificationCenter.send(
                    event = NotificationCenterEvent.PostUpdated(newPost),
                )
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

    private fun toggleDownVotePost(post: PostModel) {
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
                    auth = auth,
                    post = post,
                    downVoted = newValue,
                )
                markAsRead(newPost)
                notificationCenter.send(
                    event = NotificationCenterEvent.PostUpdated(newPost),
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handlePostUpdate(post)
            }
        }
    }

    private fun toggleSavePost(post: PostModel) {
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
                    auth = auth,
                    post = post,
                    saved = newValue,
                )
                markAsRead(newPost)
                notificationCenter.send(
                    event = NotificationCenterEvent.PostUpdated(newPost),
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handlePostUpdate(post)
            }
        }
    }

    private fun subscribe() {
        hapticFeedback.vibrate()
        screenModelScope.launch(Dispatchers.IO) {
            val community = communityRepository.subscribe(
                auth = identityRepository.authToken.value,
                id = communityId,
            )
            if (community != null) {
                updateState { it.copy(community = community) }
                notificationCenter.send(NotificationCenterEvent.CommunitySubscriptionChanged(community))
            }
        }
    }

    private fun unsubscribe() {
        hapticFeedback.vibrate()
        screenModelScope.launch(Dispatchers.IO) {
            val community = communityRepository.unsubscribe(
                auth = identityRepository.authToken.value,
                id = communityId,
            )
            if (community != null) {
                updateState { it.copy(community = community) }
                notificationCenter.send(NotificationCenterEvent.CommunitySubscriptionChanged(community))
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

    private fun handlePostDelete(id: Long) {
        updateState { it.copy(posts = it.posts.filter { post -> post.id != id }) }
    }

    private fun blockCommunity() {
        updateState { it.copy(asyncInProgress = true) }
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value
                communityRepository.block(communityId, true, auth).getOrThrow()
                emitEffect(CommunityDetailMviModel.Effect.BlockSuccess)
            } catch (e: Throwable) {
                emitEffect(CommunityDetailMviModel.Effect.BlockError(e.message))
            } finally {
                updateState { it.copy(asyncInProgress = false) }
            }
        }
    }

    private fun blockInstance() {
        updateState { it.copy(asyncInProgress = true) }
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val community = uiState.value.community
                val instanceId = community.instanceId
                val auth = identityRepository.authToken.value
                siteRepository.block(instanceId, true, auth).getOrThrow()
                emitEffect(CommunityDetailMviModel.Effect.BlockSuccess)
            } catch (e: Throwable) {
                emitEffect(CommunityDetailMviModel.Effect.BlockError(e.message))
            } finally {
                updateState { it.copy(asyncInProgress = false) }
            }
        }
    }

    private fun clearRead() {
        hideReadPosts = true
        updateState {
            val newPosts = it.posts.filter { e -> !e.read }
            it.copy(
                posts = newPosts,
            )
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

    private fun feature(post: PostModel) {
        screenModelScope.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            val newPost = postRepository.featureInCommunity(
                postId = post.id,
                auth = auth,
                featured = !post.featuredCommunity
            )
            if (newPost != null) {
                handlePostUpdate(newPost)
            }
        }
    }

    private fun lock(post: PostModel) {
        screenModelScope.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            val newPost = postRepository.lock(
                postId = post.id,
                auth = auth,
                locked = !post.locked,
            )
            if (newPost != null) {
                handlePostUpdate(newPost)
            }
        }
    }

    private fun toggleModeratorStatus(userId: Long) {
        screenModelScope.launch(Dispatchers.IO) {
            val isModerator = uiState.value.moderators.containsId(userId)
            val auth = identityRepository.authToken.value.orEmpty()
            val newModerators = communityRepository.addModerator(
                auth = auth,
                communityId = communityId,
                added = !isModerator,
                userId = userId,
            )
            updateState {
                it.copy(moderators = newModerators)
            }
        }
    }

    private fun toggleFavorite() {
        screenModelScope.launch(Dispatchers.IO) {
            val accountId = accountRepository.getActive()?.id ?: 0L
            val newValue = !uiState.value.community.favorite
            if (newValue) {
                val model = FavoriteCommunityModel(communityId = communityId)
                favoriteCommunityRepository.create(model, accountId)
            } else {
                favoriteCommunityRepository.getBy(accountId, communityId)?.also { toDelete ->
                    favoriteCommunityRepository.delete(accountId, toDelete)
                }
            }
            val newCommunity = uiState.value.community.copy(favorite = newValue)
            updateState { it.copy(community = newCommunity) }
        }
    }

    private fun updateSearchText(value: String) {
        updateState { it.copy(searchText = value) }
        screenModelScope.launch {
            searchEventChannel.send(Unit)
        }
    }
}
