package com.github.diegoberaldin.raccoonforlemmy.feature.home.postlist

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.shareUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostListViewModel(
    private val mvi: DefaultMviModel<PostListMviModel.Intent, PostListMviModel.UiState, PostListMviModel.Effect>,
    private val postRepository: PostRepository,
    private val apiConfigRepository: ApiConfigurationRepository,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val shareHelper: ShareHelper,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
) : PostListMviModel,
    MviModel<PostListMviModel.Intent, PostListMviModel.UiState, PostListMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    private var pageCursor: String? = null
    private var firstLoad = true
    private var hideReadPosts = false

    init {
        notificationCenter.addObserver(
            {
                (it as? PostModel)?.also { post ->
                    handlePostUpdate(post)
                }
            }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.PostUpdated
        )
        notificationCenter.addObserver(
            {
                (it as? PostModel)?.also { post ->
                    handlePostDelete(post.id)
                }
            }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.PostDeleted
        )
        notificationCenter.addObserver(
            {
                handleLogout()
            }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.Logout
        )
        notificationCenter.addObserver(
            {
                // apply new feed and sort type
                firstLoad = true
            }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.ResetContents
        )
    }

    fun finalize() {
        notificationCenter.removeObserver(this::class.simpleName.orEmpty())
    }

    override fun onStarted() {
        mvi.onStarted()

        mvi.scope?.launch(Dispatchers.Main) {
            apiConfigRepository.instance.onEach { instance ->
                mvi.updateState {
                    it.copy(instance = instance)
                }
            }.launchIn(this)

            identityRepository.authToken.map { !it.isNullOrEmpty() }.onEach { isLogged ->
                mvi.updateState {
                    it.copy(isLogged = isLogged)
                }
            }.launchIn(this)

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

            val auth = identityRepository.authToken.value.orEmpty()
            val user = siteRepository.getCurrentUser(auth)
            mvi.updateState { it.copy(currentUserId = user?.id ?: 0) }
        }

        if (firstLoad) {
            firstLoad = false
            val settings = settingsRepository.currentSettings.value
            mvi.updateState {
                it.copy(
                    listingType = settings.defaultListingType.toListingType(),
                    sortType = settings.defaultPostSortType.toSortType(),
                )
            }
            mvi.scope?.launch(Dispatchers.IO) {
                mvi.emitEffect(PostListMviModel.Effect.BackToTop)
                refresh()
            }
        }
    }

    override fun reduce(intent: PostListMviModel.Intent) {
        when (intent) {
            PostListMviModel.Intent.LoadNextPage -> mvi.scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }

            PostListMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
                refresh()
            }

            is PostListMviModel.Intent.ChangeSort -> applySortType(intent.value)
            is PostListMviModel.Intent.ChangeListing -> applyListingType(intent.value)
            is PostListMviModel.Intent.DownVotePost -> toggleDownVote(
                post = uiState.value.posts.first { it.id == intent.id },
                feedback = intent.feedback,
            )

            is PostListMviModel.Intent.SavePost -> toggleSave(
                post = uiState.value.posts.first { it.id == intent.id },
                feedback = intent.feedback,
            )

            is PostListMviModel.Intent.UpVotePost -> toggleUpVote(
                post = uiState.value.posts.first { it.id == intent.id },
                feedback = intent.feedback,
            )

            PostListMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is PostListMviModel.Intent.HandlePostUpdate -> handlePostUpdate(intent.post)
            is PostListMviModel.Intent.DeletePost -> handlePostDelete(intent.id)
            is PostListMviModel.Intent.SharePost -> {
                share(post = uiState.value.posts.first { it.id == intent.id })
            }

            is PostListMviModel.Intent.MarkAsRead -> {
                markAsRead(post = uiState.value.posts.first { it.id == intent.id })
            }

            PostListMviModel.Intent.ClearRead -> clearRead()
            is PostListMviModel.Intent.Hide -> hide(post = uiState.value.posts.first { it.id == intent.id })
        }
    }

    private suspend fun refresh() {
        currentPage = 1
        pageCursor = null
        hideReadPosts = false
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
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
        val type = currentState.listingType ?: ListingType.Local
        val sort = currentState.sortType ?: SortType.Active
        val refreshing = currentState.refreshing
        val includeNsfw = settingsRepository.currentSettings.value.includeNsfw
        val (itemList, nextPage) = postRepository.getAll(
            auth = auth,
            page = currentPage,
            pageCursor = pageCursor,
            type = type,
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
        }.filter { post ->
            if (includeNsfw) {
                true
            } else {
                !post.nsfw
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
                canFetchMore = itemList?.isEmpty() != true,
                refreshing = false,
            )
        }
    }

    private fun applySortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        mvi.scope?.launch {
            mvi.emitEffect(PostListMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private fun applyListingType(value: ListingType) {
        mvi.updateState { it.copy(listingType = value) }
        mvi.scope?.launch {
            mvi.emitEffect(PostListMviModel.Effect.BackToTop)
            refresh()
        }
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

    private fun handleLogout() {
        currentPage = 1
        pageCursor = null
        mvi.updateState {
            it.copy(
                posts = emptyList(),
                isLogged = false,
            )
        }
    }

    private fun handlePostDelete(id: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            postRepository.delete(id = id, auth = auth)
            handlePostDelete(id)
        }
    }

    private fun share(post: PostModel) {
        val url = post.shareUrl
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
