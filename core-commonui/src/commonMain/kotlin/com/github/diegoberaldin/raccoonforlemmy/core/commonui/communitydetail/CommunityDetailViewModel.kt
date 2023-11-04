package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.shareUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
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
    private val communityRepository: CommunityRepository,
    private val postRepository: PostRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val shareHelper: ShareHelper,
    private val hapticFeedback: HapticFeedback,
) : CommunityDetailMviModel,
    MviModel<CommunityDetailMviModel.Intent, CommunityDetailMviModel.UiState, CommunityDetailMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    private var pageCursor: String? = null
    private var hideReadPosts = false

    override fun onStarted() {
        mvi.onStarted()

        val auth = identityRepository.authToken.value.orEmpty()
        mvi.updateState {
            it.copy(
                community = community,
                isLogged = auth.isNotEmpty(),
            )
        }

        mvi.scope?.launch(Dispatchers.IO) {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)

            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        sortType = settings.defaultPostSortType.toSortType(),
                        fullHeightImages = settings.fullHeightImages,
                        separateUpAndDownVotes = settings.separateUpAndDownVotes,
                        autoLoadImages = settings.autoLoadImages,
                    )
                }
            }.launchIn(this)

            if (uiState.value.currentUserId == null) {
                val user = siteRepository.getCurrentUser(auth)
                mvi.updateState { it.copy(currentUserId = user?.id ?: 0) }
            }
            if (mvi.uiState.value.posts.isEmpty()) {
                refresh()
            }
        }
    }

    override fun reduce(intent: CommunityDetailMviModel.Intent) {
        when (intent) {
            CommunityDetailMviModel.Intent.LoadNextPage -> loadNextPage()
            CommunityDetailMviModel.Intent.Refresh -> refresh()

            is CommunityDetailMviModel.Intent.DownVotePost -> toggleDownVotePost(
                post = uiState.value.posts.first { it.id == intent.id },
                feedback = intent.feedback,
            )

            is CommunityDetailMviModel.Intent.SavePost -> toggleSavePost(
                post = uiState.value.posts.first { it.id == intent.id },
                feedback = intent.feedback,
            )

            is CommunityDetailMviModel.Intent.UpVotePost -> toggleUpVotePost(
                post = uiState.value.posts.first { it.id == intent.id },
                feedback = intent.feedback,
            )

            CommunityDetailMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is CommunityDetailMviModel.Intent.ChangeSort -> applySortType(intent.value)
            CommunityDetailMviModel.Intent.Subscribe -> subscribe()
            CommunityDetailMviModel.Intent.Unsubscribe -> unsubscribe()
            is CommunityDetailMviModel.Intent.DeletePost -> handlePostDelete(intent.id)
            is CommunityDetailMviModel.Intent.SharePost -> share(
                post = uiState.value.posts.first { it.id == intent.id },
            )

            CommunityDetailMviModel.Intent.Block -> blockCommunity()
            CommunityDetailMviModel.Intent.BlockInstance -> blockInstance()
            is CommunityDetailMviModel.Intent.MarkAsRead -> {
                markAsRead(uiState.value.posts.first { it.id == intent.id })
            }

            CommunityDetailMviModel.Intent.ClearRead -> clearRead()
            is CommunityDetailMviModel.Intent.Hide -> hide(post = uiState.value.posts.first { it.id == intent.id })
        }
    }

    private fun refresh() {
        currentPage = 1
        pageCursor = null
        hideReadPosts = false
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        val auth = identityRepository.authToken.value
        mvi.scope?.launch(Dispatchers.IO) {
            val refreshedCommunity = if (otherInstance.isNotEmpty()) {
                communityRepository.get(
                    auth = auth,
                    name = community.name,
                    instance = otherInstance,
                )
            } else {
                communityRepository.get(
                    auth = auth,
                    id = community.id,
                    name = community.name,
                )
            }
            if (refreshedCommunity != null) {
                mvi.updateState { it.copy(community = refreshedCommunity) }
            }
            loadNextPage()
        }
    }

    private fun applySortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        mvi.scope?.launch {
            mvi.emitEffect(CommunityDetailMviModel.Effect.BackToTop)
        }
        refresh()
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val refreshing = currentState.refreshing
            val sort = currentState.sortType
            val communityId = currentState.community.id
            val (itemList, nextPage) = if (otherInstance.isNotEmpty()) {
                postRepository.getAll(
                    instance = otherInstance,
                    communityId = communityId,
                    page = currentPage,
                    pageCursor = pageCursor,
                    sort = sort,
                )
            } else {
                postRepository.getAll(
                    auth = auth,
                    communityId = communityId,
                    page = currentPage,
                    pageCursor = pageCursor,
                    sort = sort,
                )
            }?.let {
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
    }

    private fun toggleUpVotePost(
        post: PostModel,
        feedback: Boolean,
    ) {
        val newValue = post.myVote <= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newPost = postRepository.asUpVoted(
            post = post,
            voted = newValue,
        )
        mvi.updateState {
            it.copy(
                posts = it.posts.map { p ->
                    if (p.id == post.id) {
                        newPost
                    } else {
                        p
                    }
                },
            )
        }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.upVote(
                    auth = auth,
                    post = post,
                    voted = newValue,
                )
                markAsRead(newPost)
            } catch (e: Throwable) {
                e.printStackTrace()
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
                mvi.updateState {
                    it.copy(
                        posts = it.posts.map { p ->
                            if (p.id == post.id) {
                                newPost
                            } else {
                                p
                            }
                        },
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
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
        }
    }

    private fun toggleDownVotePost(
        post: PostModel,
        feedback: Boolean,
    ) {
        val newValue = post.myVote >= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newPost = postRepository.asDownVoted(
            post = post,
            downVoted = newValue,
        )
        mvi.updateState {
            it.copy(
                posts = it.posts.map { p ->
                    if (p.id == post.id) {
                        newPost
                    } else {
                        p
                    }
                },
            )
        }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.downVote(
                    auth = auth,
                    post = post,
                    downVoted = newValue,
                )
                markAsRead(newPost)
            } catch (e: Throwable) {
                e.printStackTrace()
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
        }
    }

    private fun toggleSavePost(
        post: PostModel,
        feedback: Boolean,
    ) {
        val newValue = !post.saved
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newPost = postRepository.asSaved(
            post = post,
            saved = newValue,
        )
        mvi.updateState {
            it.copy(
                posts = it.posts.map { p ->
                    if (p.id == post.id) {
                        newPost
                    } else {
                        p
                    }
                },
            )
        }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.save(
                    auth = auth,
                    post = post,
                    saved = newValue,
                )
                markAsRead(newPost)
            } catch (e: Throwable) {
                e.printStackTrace()
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

    private fun handlePostDelete(id: Int) {
        mvi.updateState { it.copy(posts = it.posts.filter { post -> post.id != id }) }
    }

    private fun share(post: PostModel) {
        val url = post.shareUrl
        if (url.isNotEmpty()) {
            shareHelper.share(url, "text/plain")
        }
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
}
