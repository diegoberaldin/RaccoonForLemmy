package com.github.diegoberaldin.raccoonforlemmy.unit.explore

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
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
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ExploreViewModel(
    private val otherInstance: String,
    private val apiConfigRepository: ApiConfigurationRepository,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
    private val getSortTypesUseCase: GetSortTypesUseCase,
) : ExploreMviModel,
    DefaultMviModel<ExploreMviModel.Intent, ExploreMviModel.UiState, ExploreMviModel.Effect>(
        initialState = ExploreMviModel.UiState(),
    ) {
    private var currentPage: Int = 1
    private var searchEventChannel = Channel<Unit>()
    private val isOnOtherInstance: Boolean get() = otherInstance.isNotEmpty()
    private val notificationEventKey: String
        get() =
            buildString {
                append("explore")
                if (isOnOtherInstance) {
                    append("-")
                    append(otherInstance)
                }
            }

    init {
        updateState {
            it.copy(
                instance = apiConfigRepository.instance.value,
            )
        }
        screenModelScope.launch {
            identityRepository.isLogged.onEach { isLogged ->
                updateState {
                    it.copy(isLogged = isLogged ?: false)
                }
                updateAvailableSortTypes()
            }.launchIn(this)
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        fullHeightImages = settings.fullHeightImages,
                        fullWidthImages = settings.fullWidthImages,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        doubleTapActionEnabled = settings.enableDoubleTapAction,
                        actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts,
                        actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts,
                        actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments,
                        actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments,
                        showScores = settings.showScores,
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
                    if (evt.screenKey == notificationEventKey) {
                        changeListingType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    if (evt.screenKey == notificationEventKey) {
                        changeSortType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ResetExplore::class).onEach {
                onFirstLoad()
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSearchResultType::class).onEach { evt ->
                if (evt.screenKey == notificationEventKey) {
                    changeResultType(evt.value)
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CommunitySubscriptionChanged::class).onEach { evt ->
                handleCommunityUpdate(evt.value)
            }.launchIn(this)

            searchEventChannel.receiveAsFlow().debounce(1000).onEach {
                emitEffect(ExploreMviModel.Effect.BackToTop)
                refresh()
            }.launchIn(this)
        }

        onFirstLoad()
    }

    private fun onFirstLoad() {
        val settings = settingsRepository.currentSettings.value
        val listingType = if (isOnOtherInstance) ListingType.Local else settings.defaultExploreType.toListingType()
        val sortType = settings.defaultPostSortType.toSortType()
        updateState {
            it.copy(
                listingType = listingType,
                sortType = sortType,
            )
        }
        screenModelScope.launch {
            refresh(initial = true)
            emitEffect(ExploreMviModel.Effect.BackToTop)
        }
    }

    private suspend fun updateAvailableSortTypes() {
        val sortTypes = getSortTypesUseCase.getTypesForPosts()
        updateState { it.copy(availableSortTypes = sortTypes) }
    }

    override fun reduce(intent: ExploreMviModel.Intent) {
        when (intent) {
            ExploreMviModel.Intent.LoadNextPage -> {
                screenModelScope.launch {
                    loadNextPage()
                }
            }

            ExploreMviModel.Intent.Refresh -> {
                screenModelScope.launch {
                    refresh()
                }
            }

            ExploreMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is ExploreMviModel.Intent.SetSearch -> setSearch(intent.value)
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

            is ExploreMviModel.Intent.ToggleSubscription -> toggleSubscription(intent.communityId)
        }
    }

    private fun setSearch(value: String) {
        updateState { it.copy(searchText = value) }
        screenModelScope.launch {
            searchEventChannel.send(Unit)
        }
    }

    private fun changeListingType(value: ListingType) {
        updateState { it.copy(listingType = value) }
        screenModelScope.launch {
            emitEffect(ExploreMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private fun changeSortType(value: SortType) {
        updateState { it.copy(sortType = value) }
        screenModelScope.launch {
            emitEffect(ExploreMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private fun changeResultType(value: SearchResultType) {
        updateState { it.copy(resultType = value) }
        screenModelScope.launch {
            emitEffect(ExploreMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage = 1
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = !initial,
                loading = false,
                initial = initial,
            )
        }
        loadNextPage()
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }
        updateState { it.copy(loading = true) }
        val searchText = uiState.value.searchText
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val listingType = currentState.listingType
        val sortType = currentState.sortType
        val resultType = currentState.resultType
        val settings = settingsRepository.currentSettings.value
        val itemList =
            communityRepository.search(
                query = searchText,
                auth = auth,
                resultType = resultType,
                page = currentPage,
                listingType = listingType,
                sortType = sortType,
                instance = otherInstance,
            )
        val additionalResolvedCommunity =
            if (resultType == SearchResultType.All || resultType == SearchResultType.Communities && currentPage == 1) {
                communityRepository.getResolved(
                    query = searchText,
                    auth = auth,
                )
            } else {
                null
            }
        val additionalResolvedUser =
            if (resultType == SearchResultType.All || resultType == SearchResultType.Users && currentPage == 1) {
                userRepository.getResolved(
                    query = searchText,
                    auth = auth,
                )
            } else {
                null
            }
        if (itemList.isNotEmpty()) {
            currentPage++
        }
        val itemsToAdd =
            itemList.filter { item ->
                if (settings.includeNsfw) {
                    true
                } else {
                    isSafeForWork(item)
                }
            }.let {
                when (resultType) {
                    SearchResultType.Communities -> {
                        if (additionalResolvedCommunity != null &&
                            it.none {
                                    r ->
                                r is SearchResult.Community && r.model.id == additionalResolvedCommunity.id
                            }
                        ) {
                            it + SearchResult.Community(additionalResolvedCommunity)
                        } else {
                            it
                        }
                    }

                    SearchResultType.Users -> {
                        if (additionalResolvedUser != null &&
                            it.none {
                                    r ->
                                r is SearchResult.User && r.model.id == additionalResolvedUser.id
                            }
                        ) {
                            it + SearchResult.User(additionalResolvedUser)
                        } else {
                            it
                        }
                    }

                    SearchResultType.Posts -> {
                        if (settings.searchPostTitleOnly && searchText.isNotEmpty()) {
                            // apply the more restrictive title-only search
                            it.filterIsInstance<SearchResult.Post>()
                                .filter { r -> r.model.title.contains(other = searchText, ignoreCase = true) }
                        } else {
                            it
                        }
                    }

                    else -> it
                }
            }.filter { item ->
                if (refreshing) {
                    true
                } else {
                    // prevents accidental duplication
                    currentState.results.none { other -> getItemKey(item) == getItemKey(other) }
                }
            }
        updateState {
            val newItems =
                if (refreshing) {
                    itemsToAdd
                } else {
                    it.results + itemsToAdd
                }
            it.copy(
                results = newItems,
                loading = false,
                canFetchMore = itemList.isNotEmpty(),
                refreshing = false,
            )
        }
    }

    private fun isSafeForWork(element: SearchResult): Boolean =
        when (element) {
            is SearchResult.Community -> !element.model.nsfw
            is SearchResult.Post -> !element.model.nsfw
            is SearchResult.Comment -> true
            is SearchResult.User -> true
            else -> false
        }

    private fun handleLogout() {
        currentPage = 1
        updateState {
            it.copy(
                listingType = ListingType.Local,
                results = emptyList(),
            )
        }
        onFirstLoad()
    }

    private fun handlePostUpdate(post: PostModel) {
        updateState {
            it.copy(
                results =
                    it.results.map { r ->
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
        updateState {
            it.copy(
                results =
                    it.results.map { r ->
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
        val newPost =
            postRepository.asUpVoted(
                post = post,
                voted = newVote,
            )
        updateState {
            it.copy(
                results =
                    it.results.map { res ->
                        if (res !is SearchResult.Post) return@map res
                        if (res.model.id == post.id) {
                            res.copy(model = newPost)
                        } else {
                            res
                        }
                    },
            )
        }
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.upVote(
                    post = post,
                    auth = auth,
                    voted = newVote,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                updateState {
                    it.copy(
                        results =
                            it.results.map { res ->
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
        val newPost =
            postRepository.asDownVoted(
                post = post,
                downVoted = newValue,
            )
        updateState {
            it.copy(
                results =
                    it.results.map { res ->
                        if (res !is SearchResult.Post) return@map res
                        if (res.model.id == post.id) {
                            res.copy(model = newPost)
                        } else {
                            res
                        }
                    },
            )
        }
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.downVote(
                    post = post,
                    auth = auth,
                    downVoted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                updateState {
                    it.copy(
                        results =
                            it.results.map { res ->
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
        val newPost =
            postRepository.asSaved(
                post = post,
                saved = newValue,
            )
        updateState {
            it.copy(
                results =
                    it.results.map { res ->
                        if (res !is SearchResult.Post) return@map res
                        if (res.model.id == post.id) {
                            res.copy(model = newPost)
                        } else {
                            res
                        }
                    },
            )
        }
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.save(
                    post = post,
                    auth = auth,
                    saved = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                updateState {
                    it.copy(
                        results =
                            it.results.map { res ->
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
        val newComment =
            commentRepository.asUpVoted(
                comment = comment,
                voted = newValue,
            )
        updateState {
            it.copy(
                results =
                    it.results.map { res ->
                        if (res !is SearchResult.Comment) return@map res
                        if (res.model.id == comment.id) {
                            res.copy(model = newComment)
                        } else {
                            res
                        }
                    },
            )
        }
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.upVote(
                    auth = auth,
                    comment = comment,
                    voted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                updateState {
                    it.copy(
                        results =
                            it.results.map { res ->
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
        updateState {
            it.copy(
                results =
                    it.results.map { res ->
                        if (res !is SearchResult.Comment) return@map res
                        if (res.model.id == comment.id) {
                            res.copy(model = newComment)
                        } else {
                            res
                        }
                    },
            )
        }
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.downVote(
                    auth = auth,
                    comment = comment,
                    downVoted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                updateState {
                    it.copy(
                        results =
                            it.results.map { res ->
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
        val newComment =
            commentRepository.asSaved(
                comment = comment,
                saved = newValue,
            )
        updateState {
            it.copy(
                results =
                    it.results.map { res ->
                        if (res !is SearchResult.Comment) return@map res
                        if (res.model.id == comment.id) {
                            res.copy(model = newComment)
                        } else {
                            res
                        }
                    },
            )
        }
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.save(
                    auth = auth,
                    comment = comment,
                    saved = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                updateState {
                    it.copy(
                        results =
                            it.results.map { res ->
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

    private fun toggleSubscription(communityId: Long) {
        val community =
            uiState.value.results.firstOrNull {
                (it as? SearchResult.Community)?.model?.id == communityId
            }.let { (it as? SearchResult.Community)?.model } ?: return
        screenModelScope.launch {
            val newValue =
                when (community.subscribed) {
                    true -> {
                        hapticFeedback.vibrate()
                        communityRepository.unsubscribe(
                            auth = identityRepository.authToken.value,
                            id = communityId,
                        )
                    }

                    false -> {
                        hapticFeedback.vibrate()
                        communityRepository.subscribe(
                            auth = identityRepository.authToken.value,
                            id = communityId,
                        )
                    }

                    else -> community
                }
            if (newValue == null) {
                emitEffect(ExploreMviModel.Effect.OperationFailure)
            } else {
                handleCommunityUpdate(newValue)
            }
        }
    }

    private fun handleCommunityUpdate(community: CommunityModel) {
        updateState {
            it.copy(
                results =
                    it.results.map { res ->
                        if (res !is SearchResult.Community) return@map res
                        if (res.model.id == community.id) {
                            res.copy(model = community)
                        } else {
                            res
                        }
                    },
            )
        }
    }
}

internal fun getItemKey(result: SearchResult): String =
    when (result) {
        is SearchResult.Post -> "post" + result.model.id.toString() + result.model.updateDate
        is SearchResult.Comment -> "comment" + result.model.id.toString() + result.model.updateDate
        is SearchResult.User -> "user" + result.model.id.toString()
        is SearchResult.Community -> "community" + result.model.id.toString()
    }
