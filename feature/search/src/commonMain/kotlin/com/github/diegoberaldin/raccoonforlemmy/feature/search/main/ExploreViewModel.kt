package com.github.diegoberaldin.raccoonforlemmy.feature.search.main

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.ContentResetCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val mvi: DefaultMviModel<ExploreMviModel.Intent, ExploreMviModel.UiState, ExploreMviModel.Effect>,
    private val apiConfigRepository: ApiConfigurationRepository,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
    private val contentResetCoordinator: ContentResetCoordinator,
    private val getSortTypesUseCase: GetSortTypesUseCase,
) : ExploreMviModel,
    MviModel<ExploreMviModel.Intent, ExploreMviModel.UiState, ExploreMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    private var debounceJob: Job? = null
    private var firstLoad = true

    override fun onStarted() {
        mvi.onStarted()
        mvi.updateState {
            it.copy(
                instance = apiConfigRepository.instance.value,
            )
        }
        mvi.scope?.launch {
            identityRepository.isLogged.onEach { isLogged ->
                mvi.updateState {
                    it.copy(isLogged = isLogged ?: false)
                }
                updateAvailableSortTypes()
            }.launchIn(this)
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        fullHeightImages = settings.fullHeightImages,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        doubleTapActionEnabled = settings.enableDoubleTapAction,
                        actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts,
                        actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts,
                        actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments,
                        actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments,
                    )
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                handleLogout()
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CommentUpdated::class)
                .onEach { evt ->
                    handleCommentUpdate(evt.model)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeFeedType::class)
                .onEach { evt ->
                    changeListingType(evt.value)
                }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    changeSortType(evt.value)
                }.launchIn(this)
        }

        if (contentResetCoordinator.resetExplore) {
            contentResetCoordinator.resetExplore = false
            // apply new feed and sort type
            firstLoad = true
        }
        if (firstLoad) {
            firstLoad = false
            val settings = settingsRepository.currentSettings.value
            val listingType = settings.defaultListingType.toListingType()
            val sortType = settings.defaultPostSortType.toSortType()
            mvi.updateState {
                it.copy(
                    listingType = listingType,
                    sortType = sortType,
                )
            }
            mvi.scope?.launch(Dispatchers.IO) {
                refresh()
                mvi.emitEffect(ExploreMviModel.Effect.BackToTop)
            }
        }
    }

    private suspend fun updateAvailableSortTypes() {
        val sortTypes = getSortTypesUseCase.getTypesForPosts()
        mvi.updateState { it.copy(availableSortTypes = sortTypes) }
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

            ExploreMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is ExploreMviModel.Intent.SetSearch -> setSearch(intent.value)
            is ExploreMviModel.Intent.SetListingType -> changeListingType(intent.value)
            is ExploreMviModel.Intent.SetSortType -> changeSortType(intent.value)
            is ExploreMviModel.Intent.SetResultType -> changeResultType(intent.value)
            is ExploreMviModel.Intent.DownVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.results.firstOrNull {
                    it is SearchResult.Post && it.model.id == intent.id
                }?.also { result ->
                    toggleDownVote(
                        post = (result as SearchResult.Post).model,
                    )
                }
            }

            is ExploreMviModel.Intent.SavePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.results.firstOrNull {
                    it is SearchResult.Post && it.model.id == intent.id
                }?.also { result ->
                    toggleSave(
                        post = (result as SearchResult.Post).model,
                    )
                }
            }

            is ExploreMviModel.Intent.UpVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.results.firstOrNull {
                    it is SearchResult.Post && it.model.id == intent.id
                }?.also { result ->
                    toggleUpVote(
                        post = (result as SearchResult.Post).model,
                    )
                }
            }

            is ExploreMviModel.Intent.DownVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.results.firstOrNull {
                    it is SearchResult.Comment && it.model.id == intent.id
                }?.also { result ->
                    toggleDownVoteComment(
                        comment = (result as SearchResult.Comment).model,
                    )
                }
            }

            is ExploreMviModel.Intent.SaveComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.results.firstOrNull {
                    it is SearchResult.Comment && it.model.id == intent.id
                }?.also { result ->
                    toggleSaveComment(
                        comment = (result as SearchResult.Comment).model,
                    )
                }
            }

            is ExploreMviModel.Intent.UpVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.results.firstOrNull {
                    it is SearchResult.Comment && it.model.id == intent.id
                }?.also { result ->
                    toggleUpVoteComment(
                        comment = (result as SearchResult.Comment).model,
                    )
                }
            }
        }
    }

    private fun setSearch(value: String) {
        debounceJob?.cancel()
        mvi.updateState { it.copy(searchText = value) }
        debounceJob = mvi.scope?.launch(Dispatchers.IO) {
            delay(1_000)
            mvi.emitEffect(ExploreMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private fun changeListingType(value: ListingType) {
        mvi.updateState { it.copy(listingType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.emitEffect(ExploreMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private fun changeSortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.emitEffect(ExploreMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private fun changeResultType(value: SearchResultType) {
        mvi.updateState { it.copy(resultType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.emitEffect(ExploreMviModel.Effect.BackToTop)
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
        val settings = settingsRepository.currentSettings.value
        val itemList = communityRepository.getAll(
            query = searchText,
            auth = auth,
            resultType = resultType,
            page = currentPage,
            listingType = listingType,
            sortType = sortType,
        )
        if (!itemList.isNullOrEmpty()) {
            currentPage++
        }
        val itemsToAdd = itemList.orEmpty().filter { item ->
            if (settings.includeNsfw) {
                true
            } else {
                isSafeForWork(item)
            }
        }.let {
            if (resultType == SearchResultType.Posts && settings.searchPostTitleOnly && searchText.isNotEmpty()) {
                // apply the more restrictive title-only search
                it.filterIsInstance<SearchResult.Post>()
                    .filter { r -> r.model.title.contains(other = searchText, ignoreCase = true) }
            } else {
                it
            }
        }.filter { r1 ->
            // prevents accidental duplication
            currentState.results.none { r2 -> getItemKey(r1) == getItemKey(r2) }
        }
        mvi.updateState {
            val newItems = if (refreshing) {
                itemsToAdd
            } else {
                it.results + itemsToAdd
            }
            it.copy(
                results = newItems,
                loading = false,
                canFetchMore = itemList?.isEmpty() != true,
                refreshing = false,
            )
        }
    }

    private fun isSafeForWork(element: SearchResult): Boolean = when (element) {
        is SearchResult.Community -> !element.model.nsfw
        is SearchResult.Post -> !element.model.nsfw
        is SearchResult.Comment -> true
        is SearchResult.User -> true
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

    private fun handlePostUpdate(post: PostModel) {
        mvi.updateState {
            it.copy(
                results = it.results.map { r ->
                    if (r is SearchResult.Post && r.model.id == post.id) {
                        r.copy(model = post)
                    } else {
                        r
                    }
                },
            )
        }
    }

    private fun handleCommentUpdate(comment: CommentModel) {
        mvi.updateState {
            it.copy(
                results = it.results.map { r ->
                    if (r is SearchResult.Comment && r.model.id == comment.id) {
                        r.copy(model = comment)
                    } else {
                        r
                    }
                },
            )
        }
    }

    private fun toggleUpVote(post: PostModel) {
        val newVote = post.myVote <= 0
        val newPost = postRepository.asUpVoted(
            post = post,
            voted = newVote,
        )
        mvi.updateState {
            it.copy(
                results = it.results.map { res ->
                    if (res !is SearchResult.Post) return@map res
                    if (res.model.id == post.id) {
                        res.copy(model = newPost)
                    } else {
                        res
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
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
                    it.copy(
                        results = it.results.map { res ->
                            if (res !is SearchResult.Post) return@map res
                            if (res.model.id == post.id) {
                                res.copy(model = post)
                            } else {
                                res
                            }
                        },
                    )
                }
            }
        }
    }

    private fun toggleDownVote(post: PostModel) {
        val newValue = post.myVote >= 0
        val newPost = postRepository.asDownVoted(
            post = post,
            downVoted = newValue,
        )
        mvi.updateState {
            it.copy(
                results = it.results.map { res ->
                    if (res !is SearchResult.Post) return@map res
                    if (res.model.id == post.id) {
                        res.copy(model = newPost)
                    } else {
                        res
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
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
                    it.copy(
                        results = it.results.map { res ->
                            if (res !is SearchResult.Post) return@map res
                            if (res.model.id == post.id) {
                                res.copy(model = post)
                            } else {
                                res
                            }
                        },
                    )
                }
            }
        }
    }

    private fun toggleSave(post: PostModel) {
        val newValue = !post.saved
        val newPost = postRepository.asSaved(
            post = post,
            saved = newValue,
        )
        mvi.updateState {
            it.copy(
                results = it.results.map { res ->
                    if (res !is SearchResult.Post) return@map res
                    if (res.model.id == post.id) {
                        res.copy(model = newPost)
                    } else {
                        res
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
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
                    it.copy(
                        results = it.results.map { res ->
                            if (res !is SearchResult.Post) return@map res
                            if (res.model.id == post.id) {
                                res.copy(model = post)
                            } else {
                                res
                            }
                        },
                    )
                }
            }
        }
    }

    private fun toggleUpVoteComment(comment: CommentModel) {
        val newValue = comment.myVote <= 0
        val newComment = commentRepository.asUpVoted(
            comment = comment,
            voted = newValue,
        )
        mvi.updateState {
            it.copy(
                results = it.results.map { res ->
                    if (res !is SearchResult.Comment) return@map res
                    if (res.model.id == comment.id) {
                        res.copy(model = newComment)
                    } else {
                        res
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
                        results = it.results.map { res ->
                            if (res !is SearchResult.Comment) return@map res
                            if (res.model.id == comment.id) {
                                res.copy(model = comment)
                            } else {
                                res
                            }
                        },
                    )
                }
            }
        }
    }

    private fun toggleDownVoteComment(comment: CommentModel) {
        val newValue = comment.myVote >= 0
        val newComment = commentRepository.asDownVoted(comment, newValue)
        mvi.updateState {
            it.copy(
                results = it.results.map { res ->
                    if (res !is SearchResult.Comment) return@map res
                    if (res.model.id == comment.id) {
                        res.copy(model = newComment)
                    } else {
                        res
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
                        results = it.results.map { res ->
                            if (res !is SearchResult.Comment) return@map res
                            if (res.model.id == comment.id) {
                                res.copy(model = comment)
                            } else {
                                res
                            }
                        },
                    )
                }
            }
        }
    }

    private fun toggleSaveComment(comment: CommentModel) {
        val newValue = !comment.saved
        val newComment = commentRepository.asSaved(
            comment = comment,
            saved = newValue,
        )
        mvi.updateState {
            it.copy(
                results = it.results.map { res ->
                    if (res !is SearchResult.Comment) return@map res
                    if (res.model.id == comment.id) {
                        res.copy(model = newComment)
                    } else {
                        res
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
                        results = it.results.map { res ->
                            if (res !is SearchResult.Comment) return@map res
                            if (res.model.id == comment.id) {
                                res.copy(model = comment)
                            } else {
                                res
                            }
                        },
                    )
                }
            }
        }
    }
}

internal fun getItemKey(result: SearchResult): String = when (result) {
    is SearchResult.Post -> "post" + result.model.id.toString() + result.model.updateDate
    is SearchResult.Comment -> "comment" + result.model.id.toString() + result.model.updateDate
    is SearchResult.User -> "user" + result.model.id.toString()
    is SearchResult.Community -> "community" + result.model.id.toString()
}