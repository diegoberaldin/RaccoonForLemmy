package com.github.diegoberaldin.raccoonforlemmy.feature.search.main

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

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
        mvi.scope?.launch(Dispatchers.Main) {
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
                        separateUpAndDownVotes = settings.separateUpAndDownVotes,
                        autoLoadImages = settings.autoLoadImages,
                        fullHeightImages = settings.fullHeightImages,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        doubleTapActionEnabled = settings.enableDoubleTapAction,
                    )
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                handleLogout()
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ResetContents::class).onEach {
                // apply feed and sort type
                firstLoad = true
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CommentUpdated::class)
                .onEach { evt ->
                    handleCommentUpdate(evt.model)
                }.launchIn(this)
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
                mvi.emitEffect(ExploreMviModel.Effect.BackToTop)
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

            ExploreMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is ExploreMviModel.Intent.SetSearch -> setSearch(intent.value)
            is ExploreMviModel.Intent.SetListingType -> changeListingType(intent.value)
            is ExploreMviModel.Intent.SetSortType -> changeSortType(intent.value)
            is ExploreMviModel.Intent.SetResultType -> changeResultType(intent.value)
            is ExploreMviModel.Intent.DownVotePost -> {
                uiState.value.results.firstOrNull { (it as? PostModel)?.id == intent.id }
                    ?.also { post ->
                        toggleDownVote(
                            post = post as PostModel,
                            feedback = intent.feedback,
                        )
                    }
            }

            is ExploreMviModel.Intent.SavePost -> {
                uiState.value.results.firstOrNull { (it as? PostModel)?.id == intent.id }
                    ?.also { post ->
                        toggleSave(
                            post = post as PostModel,
                            feedback = intent.feedback,
                        )
                    }
            }

            is ExploreMviModel.Intent.UpVotePost -> {
                uiState.value.results.firstOrNull { (it as? PostModel)?.id == intent.id }
                    ?.also { post ->
                        toggleUpVote(
                            post = post as PostModel,
                            feedback = intent.feedback,
                        )
                    }
            }

            is ExploreMviModel.Intent.DownVoteComment -> {
                uiState.value.results.firstOrNull { (it as? CommentModel)?.id == intent.id }
                    ?.also { comment ->
                        toggleDownVoteComment(
                            comment = comment as CommentModel,
                            feedback = intent.feedback,
                        )
                    }
            }

            is ExploreMviModel.Intent.SaveComment -> {
                uiState.value.results.firstOrNull { (it as? CommentModel)?.id == intent.id }
                    ?.also { comment ->
                        toggleSaveComment(
                            comment = comment as CommentModel,
                            feedback = intent.feedback,
                        )
                    }
            }

            is ExploreMviModel.Intent.UpVoteComment -> {
                uiState.value.results.firstOrNull { (it as? CommentModel)?.id == intent.id }
                    ?.also { comment ->
                        toggleUpVoteComment(
                            comment = comment as CommentModel,
                            feedback = intent.feedback,
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

    private fun isSafeForWork(element: Any): Boolean = when (element) {
        is CommunityModel -> element.nsfw
        is PostModel -> element.nsfw
        is CommentModel -> true
        is UserModel -> true
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
                    if (r is PostModel && r.id == post.id) {
                        post
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
                    if (r is CommentModel && r.id == comment.id) {
                        comment
                    } else {
                        r
                    }
                },
            )
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
                postRepository.upVote(
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
        val newPost = postRepository.asDownVoted(
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
                postRepository.downVote(
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
        val newPost = postRepository.asSaved(
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
                postRepository.save(
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

@OptIn(ExperimentalEncodingApi::class)
internal fun getItemKey(result: Any): String = when (result) {
    is PostModel -> "post" + result.id.toString() + result.updateDate
    is CommentModel -> "comment" + result.id.toString() + result.updateDate
    is UserModel -> "user" + result.id.toString()
    is CommunityModel -> "community" + result.id.toString()
    else -> {
        val key = ByteArray(64)
        Random(0).nextBytes(key)
        Base64.encode(key)
    }
}