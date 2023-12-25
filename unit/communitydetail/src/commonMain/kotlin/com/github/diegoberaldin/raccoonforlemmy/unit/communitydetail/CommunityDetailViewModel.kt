package com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.FavoriteCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
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
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.containsId
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CommunityDetailViewModel(
    private val mvi: DefaultMviModel<CommunityDetailMviModel.Intent, CommunityDetailMviModel.UiState, CommunityDetailMviModel.Effect>,
    private val community: CommunityModel,
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
) : CommunityDetailMviModel,
    MviModel<CommunityDetailMviModel.Intent, CommunityDetailMviModel.UiState, CommunityDetailMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    private var pageCursor: String? = null
    private var hideReadPosts = false

    override fun onStarted() {
        mvi.onStarted()

        mvi.updateState {
            it.copy(
                community = it.community.takeIf { c -> c.id != 0 } ?: community,
                instance = otherInstance.takeIf { n -> n.isNotEmpty() }
                    ?: apiConfigurationRepository.instance.value,
            )
        }

        mvi.scope?.launch(Dispatchers.IO) {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            identityRepository.isLogged.onEach { logged ->
                mvi.updateState { it.copy(isLogged = logged ?: false) }
            }.launchIn(this)

            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        doubleTapActionEnabled = settings.enableDoubleTapAction,
                        sortType = settings.defaultPostSortType.toSortType(),
                        fullHeightImages = settings.fullHeightImages,
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                    )
                }
            }.launchIn(this)

            zombieModeHelper.index.onEach { index ->
                if (mvi.uiState.value.zombieModeActive) {
                    mvi.emitEffect(CommunityDetailMviModel.Effect.ZombieModeTick(index))
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
                    mvi.updateState {
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
                    applySortType(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Share::class).onEach { evt ->
                shareHelper.share(evt.url)
            }.launchIn(this)

            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                mvi.updateState { it.copy(currentUserId = user?.id ?: 0) }
            }
            if (mvi.uiState.value.posts.isEmpty()) {
                val sortTypes = getSortTypesUseCase.getTypesForPosts(otherInstance = otherInstance)
                mvi.updateState { it.copy(availableSortTypes = sortTypes) }
                refresh()
            }
        }
    }

    override fun reduce(intent: CommunityDetailMviModel.Intent) {
        when (intent) {
            CommunityDetailMviModel.Intent.LoadNextPage -> mvi.scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }

            CommunityDetailMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
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
            is CommunityDetailMviModel.Intent.ChangeSort -> applySortType(intent.value)
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
                mvi.updateState { it.copy(zombieModeActive = false) }
                zombieModeHelper.pause()
            }

            is CommunityDetailMviModel.Intent.StartZombieMode -> {
                mvi.updateState { it.copy(zombieModeActive = true) }
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
        }
    }

    private suspend fun refresh() {
        currentPage = 1
        pageCursor = null
        hideReadPosts = false
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
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
            mvi.updateState {
                it.copy(
                    community = refreshedCommunity,
                    moderators = moderators,
                )
            }
        }

        loadNextPage()
    }

    private fun applySortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.emitEffect(CommunityDetailMviModel.Effect.BackToTop)
            refresh()
        }
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
        val sort = currentState.sortType
        val communityId = currentState.community.id
        val (itemList, nextPage) = postRepository.getAll(
            auth = auth,
            otherInstance = otherInstance,
            communityId = communityId,
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
        if (!itemList.isNullOrEmpty()) {
            currentPage++
        }
        if (nextPage != null) {
            pageCursor = nextPage
        }
        val itemsToAdd = itemList.orEmpty().filter { post ->
            if (hideReadPosts) {
                !post.read
            } else {
                true
            }
        }
        if (uiState.value.autoLoadImages) {
            itemsToAdd.forEach { post ->
                post.imageUrl.takeIf { i -> i.isNotEmpty() }?.also { url ->
                    imagePreloadManager.preload(url)
                }
            }
        }
        mvi.updateState {
            val newItems = if (refreshing) {
                itemsToAdd
            } else {
                it.posts + itemsToAdd
            }
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
            communityRepository.subscribe(
                auth = identityRepository.authToken.value,
                id = community.id,
            )
            // the first response isn't immediately true, simulate here
            mvi.updateState { it.copy(community = it.community.copy(subscribed = true)) }
        }
    }

    private fun unsubscribe() {
        hapticFeedback.vibrate()
        mvi.scope?.launch(Dispatchers.IO) {
            val community = communityRepository.unsubscribe(
                auth = identityRepository.authToken.value,
                id = community.id,
            )
            if (community != null) {
                mvi.updateState { it.copy(community = community) }
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

    private fun handlePostDelete(id: Int) {
        mvi.updateState { it.copy(posts = it.posts.filter { post -> post.id != id }) }
    }

    private fun blockCommunity() {
        mvi.updateState { it.copy(asyncInProgress = true) }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val communityId = community.id
                val auth = identityRepository.authToken.value
                communityRepository.block(communityId, true, auth).getOrThrow()
                mvi.emitEffect(CommunityDetailMviModel.Effect.BlockSuccess)
            } catch (e: Throwable) {
                mvi.emitEffect(CommunityDetailMviModel.Effect.BlockError(e.message))
            } finally {
                mvi.updateState { it.copy(asyncInProgress = false) }
            }
        }
    }

    private fun blockInstance() {
        mvi.updateState { it.copy(asyncInProgress = true) }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val instanceId = community.instanceId
                val auth = identityRepository.authToken.value
                siteRepository.block(instanceId, true, auth).getOrThrow()
                mvi.emitEffect(CommunityDetailMviModel.Effect.BlockSuccess)
            } catch (e: Throwable) {
                mvi.emitEffect(CommunityDetailMviModel.Effect.BlockError(e.message))
            } finally {
                mvi.updateState { it.copy(asyncInProgress = false) }
            }
        }
    }

    private fun clearRead() {
        hideReadPosts = true
        mvi.updateState {
            val newPosts = it.posts.filter { e -> !e.read }
            it.copy(
                posts = newPosts,
            )
        }
    }

    private fun hide(post: PostModel) {
        mvi.updateState {
            val newPosts = it.posts.filter { e -> e.id != post.id }
            it.copy(
                posts = newPosts,
            )
        }
        markAsRead(post)
    }

    private fun feature(post: PostModel) {
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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

    private fun toggleModeratorStatus(userId: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val isModerator = uiState.value.moderators.containsId(userId)
            val auth = identityRepository.authToken.value.orEmpty()
            val newModerators = communityRepository.addModerator(
                auth = auth,
                communityId = community.id,
                added = !isModerator,
                userId = userId,
            )
            mvi.updateState {
                it.copy(moderators = newModerators)
            }
        }
    }

    private fun toggleFavorite() {
        val communityId = community.id
        mvi.scope?.launch(Dispatchers.IO) {
            val accountId = accountRepository.getActive()?.id ?: 0L
            val newValue = !community.favorite
            if (newValue) {
                val model = FavoriteCommunityModel(communityId = communityId)
                favoriteCommunityRepository.create(model, accountId)
            } else {
                favoriteCommunityRepository.getBy(accountId, communityId)?.also { toDelete ->
                    favoriteCommunityRepository.delete(accountId, toDelete)
                }
            }
            val newCommunity = uiState.value.community.copy(favorite = newValue)
            mvi.updateState { it.copy(community = newCommunity) }
        }
    }
}
