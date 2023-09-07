package com.github.diegoberaldin.raccoonforlemmy.feature.home.postlist

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.KeyStoreKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostListViewModel(
    private val mvi: DefaultMviModel<PostListMviModel.Intent, PostListMviModel.UiState, PostListMviModel.Effect>,
    private val postsRepository: PostsRepository,
    private val apiConfigRepository: ApiConfigurationRepository,
    private val identityRepository: IdentityRepository,
    private val keyStore: TemporaryKeyStore,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
) : ScreenModel,
    MviModel<PostListMviModel.Intent, PostListMviModel.UiState, PostListMviModel.Effect> by mvi {

    private var currentPage: Int = 1

    override fun reduce(intent: PostListMviModel.Intent) {
        when (intent) {
            PostListMviModel.Intent.LoadNextPage -> loadNextPage()
            PostListMviModel.Intent.Refresh -> refresh()
            is PostListMviModel.Intent.ChangeSort -> applySortType(intent.value)
            is PostListMviModel.Intent.ChangeListing -> applyListingType(intent.value)
            is PostListMviModel.Intent.DownVotePost -> toggleDownVote(
                post = intent.post,
                feedback = intent.feedback,
            )

            is PostListMviModel.Intent.SavePost -> toggleSave(
                post = intent.post,
                feedback = intent.feedback,
            )

            is PostListMviModel.Intent.UpVotePost -> toggleUpVote(
                post = intent.post,
                feedback = intent.feedback,
            )

            PostListMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
        }
    }

    override fun onDisposed() {
        mvi.onDisposed()
    }

    override fun onStarted() {
        mvi.onStarted()
        val listingType = keyStore[KeyStoreKeys.DefaultListingType, 0].toListingType()
        val sortType = keyStore[KeyStoreKeys.DefaultPostSortType, 0].toSortType()
        mvi.updateState {
            it.copy(
                instance = apiConfigRepository.getInstance(),
                listingType = listingType,
                sortType = sortType,
                blurNsfw = keyStore[KeyStoreKeys.BlurNsfw, true],
            )
        }

        mvi.scope.launch(Dispatchers.Main) {
            identityRepository.authToken.map { !it.isNullOrEmpty() }.onEach { isLogged ->
                mvi.updateState {
                    it.copy(isLogged = isLogged)
                }
            }.launchIn(this)
            notificationCenter.events.filterIsInstance<NotificationCenter.Event.PostUpdate>()
                .onEach { evt ->
                    val newPost = evt.post
                    mvi.updateState {
                        it.copy(
                            posts = it.posts.map { p ->
                                if (p.id == newPost.id) {
                                    newPost
                                } else {
                                    p
                                }
                            },
                        )
                    }
                }.launchIn(this)
        }

        if (mvi.uiState.value.posts.isEmpty()) {
            refresh()
        }
    }

    private fun refresh() {
        currentPage = 1
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val type = currentState.listingType
            val sort = currentState.sortType
            val refreshing = currentState.refreshing
            val includeNsfw = keyStore[KeyStoreKeys.IncludeNsfw, true]
            val postList = postsRepository.getAll(
                auth = auth,
                page = currentPage,
                type = type,
                sort = sort,
            )
            currentPage++
            val canFetchMore = postList.size >= PostsRepository.DEFAULT_PAGE_SIZE
            mvi.updateState {
                val newPosts = if (refreshing) {
                    postList
                } else {
                    it.posts + postList
                }.filter { post ->
                    if (includeNsfw) {
                        true
                    } else {
                        !post.nsfw
                    }
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

    private fun applyListingType(value: ListingType) {
        mvi.updateState { it.copy(listingType = value) }
        refresh()
    }

    private fun toggleUpVote(post: PostModel, feedback: Boolean) {
        val newVote = post.myVote <= 0
        val newPost = postsRepository.asUpVoted(
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
        mvi.scope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postsRepository.upVote(
                    post = post,
                    auth = auth,
                    voted = newVote,
                )
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
        val newPost = postsRepository.asDownVoted(
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
        mvi.scope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postsRepository.downVote(
                    post = post,
                    auth = auth,
                    downVoted = newValue,
                )
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
        val newPost = postsRepository.asSaved(
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
        mvi.scope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postsRepository.save(
                    post = post,
                    auth = auth,
                    saved = newValue,
                )
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
}
