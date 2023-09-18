package com.github.diegoberaldin.raccoonforlemmy.feature.search.content

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.KeyStoreKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ExporeViewModel(
    private val mvi: DefaultMviModel<ExploreMviModel.Intent, ExploreMviModel.UiState, ExploreMviModel.Effect>,
    private val apiConfigRepository: ApiConfigurationRepository,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val postsRepository: PostsRepository,
    private val commentRepository: CommentRepository,
    private val keyStore: TemporaryKeyStore,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
) : ScreenModel,
    MviModel<ExploreMviModel.Intent, ExploreMviModel.UiState, ExploreMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    private var debounceJob: Job? = null

    init {
        notificationCenter.addObserver({
            handleLogout()
        }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.Logout)
    }

    fun finalize() {
        notificationCenter.removeObserver(this::class.simpleName.orEmpty())
    }

    override fun onStarted() {
        mvi.onStarted()
        val blurNsfw = keyStore[KeyStoreKeys.BlurNsfw, true]
        mvi.updateState {
            it.copy(
                instance = apiConfigRepository.getInstance(),
                blurNsfw = blurNsfw,
            )
        }
        mvi.scope?.launch(Dispatchers.Main) {
            identityRepository.authToken.map { !it.isNullOrEmpty() }.onEach { isLogged ->
                mvi.updateState {
                    it.copy(isLogged = isLogged)
                }
            }.launchIn(this)
        }

        if (mvi.uiState.value.results.isEmpty()) {
            mvi.scope?.launch(Dispatchers.IO) {
                refresh()
            }
        }
    }

    override fun reduce(intent: ExploreMviModel.Intent) {
        when (intent) {
            ExploreMviModel.Intent.LoadNextPage -> {
                mvi.scope?.launch(Dispatchers.IO) {
                    loadNextPage()
                }
            }

            ExploreMviModel.Intent.Refresh -> {
                mvi.scope?.launch(Dispatchers.IO) {
                    refresh()
                }
            }

            is ExploreMviModel.Intent.SetSearch -> setSearch(intent.value)
            is ExploreMviModel.Intent.SetListingType -> changeListingType(intent.value)
            is ExploreMviModel.Intent.SetSortType -> changeSortType(intent.value)
            is ExploreMviModel.Intent.SetResultType -> changeResultType(intent.value)
            is ExploreMviModel.Intent.DownVotePost -> toggleDownVote(
                post = uiState.value.results[intent.index] as PostModel,
                feedback = intent.feedback,
            )

            is ExploreMviModel.Intent.SavePost -> toggleSave(
                post = uiState.value.results[intent.index] as PostModel,
                feedback = intent.feedback,
            )

            is ExploreMviModel.Intent.UpVotePost -> toggleUpVote(
                post = uiState.value.results[intent.index] as PostModel,
                feedback = intent.feedback,
            )

            is ExploreMviModel.Intent.DownVoteComment -> toggleDownVoteComment(
                comment = uiState.value.results[intent.index] as CommentModel,
                feedback = intent.feedback,
            )

            is ExploreMviModel.Intent.SaveComment -> toggleSaveComment(
                comment = uiState.value.results[intent.index] as CommentModel,
                feedback = intent.feedback,
            )

            is ExploreMviModel.Intent.UpVoteComment -> toggleUpVoteComment(
                comment = uiState.value.results[intent.index] as CommentModel,
                feedback = intent.feedback,
            )
        }
    }

    private fun setSearch(value: String) {
        debounceJob?.cancel()
        mvi.updateState { it.copy(searchText = value) }
        debounceJob = mvi.scope?.launch(Dispatchers.IO) {
            delay(1_000)
            refresh()
        }
    }

    private fun changeListingType(value: ListingType) {
        mvi.updateState { it.copy(listingType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            refresh()
        }
    }

    private fun changeSortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            refresh()
        }
    }

    private fun changeResultType(value: SearchResultType) {
        mvi.updateState { it.copy(resultType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            refresh()
        }
    }

    private suspend fun refresh() {
        currentPage = 1
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
        val searchText = mvi.uiState.value.searchText
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val listingType = currentState.listingType
        val sortType = currentState.sortType
        val resultType = currentState.resultType
        val inclueNsfw = keyStore[KeyStoreKeys.IncludeNsfw, true]
        val items = communityRepository.getAll(
            query = searchText,
            auth = auth,
            resultType = resultType,
            page = currentPage,
            listingType = listingType,
            sortType = sortType,
        )
        currentPage++
        val canFetchMore = items.size >= PostsRepository.DEFAULT_PAGE_SIZE
        mvi.updateState {
            val newItems = if (refreshing) {
                items
            } else {
                it.results + items
            }.filter { community ->
                if (inclueNsfw) {
                    true
                } else {
                    isSafeForWork(community)
                }
            }
            it.copy(
                results = newItems,
                loading = false,
                canFetchMore = canFetchMore,
                refreshing = false,
            )
        }
    }

    private fun isSafeForWork(element: Any): Boolean = when (element) {
        is CommunityModel -> element.nsfw
        else -> false
    }

    private fun handleLogout() {
        currentPage = 1
        mvi.updateState {
            it.copy(
                listingType = ListingType.Local,
                results = emptyList(),
            )
        }
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
                results = it.results.map { p ->
                    if (p !is PostModel) return@map p
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
                postsRepository.upVote(
                    post = post,
                    auth = auth,
                    voted = newVote,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
                    it.copy(
                        results = it.results.map { p ->
                            if (p !is PostModel) return@map p
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
                results = it.results.map { p ->
                    if (p !is PostModel) return@map p
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
                postsRepository.downVote(
                    post = post,
                    auth = auth,
                    downVoted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
                    it.copy(
                        results = it.results.map { p ->
                            if (p !is PostModel) return@map p
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
                results = it.results.map { p ->
                    if (p !is PostModel) return@map p
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
                postsRepository.save(
                    post = post,
                    auth = auth,
                    saved = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
                    it.copy(
                        results = it.results.map { p ->
                            if (p !is PostModel) return@map p
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

    private fun toggleUpVoteComment(
        comment: CommentModel,
        feedback: Boolean,
    ) {
        val newValue = comment.myVote <= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newComment = commentRepository.asUpVoted(
            comment = comment,
            voted = newValue,
        )
        mvi.updateState {
            it.copy(
                results = it.results.map { c ->
                    if (c !is CommentModel) return@map it
                    if (c.id == comment.id) {
                        newComment
                    } else {
                        c
                    }
                },
            )
        }
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
                mvi.updateState {
                    it.copy(
                        results = it.results.map { c ->
                            if (c !is CommentModel) return@map it
                            if (c.id == comment.id) {
                                comment
                            } else {
                                c
                            }
                        },
                    )
                }
            }
        }
    }

    private fun toggleDownVoteComment(
        comment: CommentModel,
        feedback: Boolean,
    ) {
        val newValue = comment.myVote >= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newComment = commentRepository.asDownVoted(comment, newValue)
        mvi.updateState {
            it.copy(
                results = it.results.map { c ->
                    if (c !is CommentModel) return@map it
                    if (c.id == comment.id) {
                        newComment
                    } else {
                        c
                    }
                },
            )
        }
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
                mvi.updateState {
                    it.copy(
                        results = it.results.map { c ->
                            if (c !is CommentModel) return@map it
                            if (c.id == comment.id) {
                                comment
                            } else {
                                c
                            }
                        },
                    )
                }
            }
        }
    }

    private fun toggleSaveComment(
        comment: CommentModel,
        feedback: Boolean,
    ) {
        val newValue = !comment.saved
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newComment = commentRepository.asSaved(
            comment = comment,
            saved = newValue,
        )
        mvi.updateState {
            it.copy(
                results = it.results.map { c ->
                    if (c !is CommentModel) return@map it
                    if (c.id == comment.id) {
                        newComment
                    } else {
                        c
                    }
                },
            )
        }
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
                mvi.updateState {
                    it.copy(
                        results = it.results.map { c ->
                            if (c !is CommentModel) return@map it
                            if (c.id == comment.id) {
                                comment
                            } else {
                                c
                            }
                        },
                    )
                }
            }
        }
    }
}