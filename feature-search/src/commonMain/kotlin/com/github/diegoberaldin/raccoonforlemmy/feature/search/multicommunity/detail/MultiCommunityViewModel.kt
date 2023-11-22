package com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.utils.MultiCommunityPaginator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MultiCommunityViewModel(
    private val mvi: DefaultMviModel<MultiCommunityMviModel.Intent, MultiCommunityMviModel.UiState, MultiCommunityMviModel.Effect>,
    private val community: MultiCommunityModel,
    private val postRepository: PostRepository,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val shareHelper: ShareHelper,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
    private val paginator: MultiCommunityPaginator,
    private val imagePreloadManager: ImagePreloadManager,
) : MultiCommunityMviModel,
    MviModel<MultiCommunityMviModel.Intent, MultiCommunityMviModel.UiState, MultiCommunityMviModel.Effect> by mvi {

    private var hideReadPosts = false

    override fun onStarted() {
        mvi.onStarted()

        mvi.scope?.launch {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)

            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        separateUpAndDownVotes = settings.separateUpAndDownVotes,
                        autoLoadImages = settings.autoLoadImages,
                        fullHeightImages = settings.fullHeightImages,
                    )
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)

            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                mvi.updateState { it.copy(currentUserId = user?.id ?: 0) }
            }
        }

        mvi.scope?.launch(Dispatchers.IO) {
            if (uiState.value.posts.isEmpty()) {
                val settings = settingsRepository.currentSettings.value
                mvi.updateState {
                    it.copy(
                        sortType = settings.defaultPostSortType.toSortType(),
                    )
                }
                paginator.setCommunities(community.communityIds)
                refresh()
            }
        }
    }

    override fun reduce(intent: MultiCommunityMviModel.Intent) {
        when (intent) {
            is MultiCommunityMviModel.Intent.ChangeSort -> applySortType(intent.value)
            is MultiCommunityMviModel.Intent.DownVotePost -> toggleDownVote(
                post = uiState.value.posts.first { it.id == intent.id },
                feedback = intent.feedback,
            )

            MultiCommunityMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            MultiCommunityMviModel.Intent.LoadNextPage -> loadNextPage()
            MultiCommunityMviModel.Intent.Refresh -> refresh()
            is MultiCommunityMviModel.Intent.SavePost -> toggleSave(
                post = uiState.value.posts.first { it.id == intent.id },
                feedback = intent.feedback,
            )

            is MultiCommunityMviModel.Intent.SharePost -> share(
                post = uiState.value.posts.first { it.id == intent.id }
            )

            is MultiCommunityMviModel.Intent.UpVotePost -> toggleUpVote(
                post = uiState.value.posts.first { it.id == intent.id },
                feedback = intent.feedback,
            )

            MultiCommunityMviModel.Intent.ClearRead -> clearRead()
            is MultiCommunityMviModel.Intent.MarkAsRead -> markAsRead(
                post = uiState.value.posts.first { it.id == intent.id })

            is MultiCommunityMviModel.Intent.Hide -> hide(
                post = uiState.value.posts.first { it.id == intent.id })
        }
    }

    private fun refresh() {
        hideReadPosts = false
        paginator.reset()
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
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
            val sort = currentState.sortType ?: SortType.Active
            val refreshing = currentState.refreshing
            val includeNsfw = settingsRepository.currentSettings.value.includeNsfw

            val itemList = paginator.loadNextPage(
                auth = auth,
                sort = sort,
                currentIds = if (refreshing) emptyList() else currentState.posts.map { it.id }
            )
            val canFetchMore = paginator.canFetchMore
            val itemsToAdd = itemList.filter { post ->
                if (includeNsfw) {
                    true
                } else {
                    !post.nsfw
                }
            }.filter { post ->
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
                )
            }
        }
    }

    private fun applySortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        refresh()
    }

    private fun toggleUpVote(post: PostModel, feedback: Boolean) {
        val newVote = post.myVote <= 0
        val newPost = postRepository.asUpVoted(
            post = post,
            voted = newVote,
        )
        if (feedback) {
            hapticFeedback.vibrate()
        }
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
                    post = post,
                    auth = auth,
                    voted = newVote,
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

    private fun toggleDownVote(post: PostModel, feedback: Boolean) {
        val newValue = post.myVote >= 0
        val newPost = postRepository.asDownVoted(
            post = post,
            downVoted = newValue,
        )
        if (feedback) {
            hapticFeedback.vibrate()
        }
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
                    post = post,
                    auth = auth,
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

    private fun toggleSave(post: PostModel, feedback: Boolean) {
        val newValue = !post.saved
        val newPost = postRepository.asSaved(
            post = post,
            saved = newValue,
        )
        if (feedback) {
            hapticFeedback.vibrate()
        }
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
                    post = post,
                    auth = auth,
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

    private fun share(post: PostModel) {
        val url = post.originalUrl.orEmpty()
        if (url.isNotEmpty()) {
            shareHelper.share(url, "text/plain")
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
