package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail

import cafe.adriel.voyager.core.model.screenModelScope
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommentPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommentPaginationSpecification
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.PostNavigationManager
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.containsId
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.utils.populateLoadMoreComments
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.utils.sortToNestedOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class PostDetailViewModel(
    postId: Long,
    private val otherInstance: String,
    private val highlightCommentId: Long?,
    private val isModerator: Boolean,
    private val identityRepository: IdentityRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val postRepository: PostRepository,
    private val commentPaginationManager: CommentPaginationManager,
    private val commentRepository: CommentRepository,
    private val communityRepository: CommunityRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val shareHelper: ShareHelper,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
    private val getSortTypesUseCase: GetSortTypesUseCase,
    private val itemCache: LemmyItemCache,
    private val postNavigationManager: PostNavigationManager,
) : PostDetailMviModel,
    DefaultMviModel<PostDetailMviModel.Intent, PostDetailMviModel.UiState, PostDetailMviModel.Effect>(
        initialState = PostDetailMviModel.UiState(),
    ) {
    private var highlightCommentPath: String? = null
    private var commentWasHighlighted = false
    private val searchEventChannel = Channel<Unit>()
    private val initialNavigationEnabled = postNavigationManager.canNavigate.value

    override fun onDispose() {
        super.onDispose()
        if (initialNavigationEnabled) {
            postNavigationManager.pop()
        }
    }

    init {
        screenModelScope.launch {
            updateState {
                it.copy(
                    instance =
                        otherInstance.takeIf { n -> n.isNotEmpty() }
                            ?: apiConfigurationRepository.instance.value,
                )
            }
            if (uiState.value.post.id == 0L) {
                val post = itemCache.getPost(postId) ?: PostModel()
                val downVoteEnabled = siteRepository.isDownVoteEnabled(identityRepository.authToken.value)
                updateState {
                    it.copy(
                        post = post,
                        isModerator = isModerator,
                        currentUserId = identityRepository.cachedUser?.id,
                        isAdmin = identityRepository.cachedUser?.admin == true,
                        downVoteEnabled = downVoteEnabled,
                    )
                }
            }

            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)

            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        swipeActionsEnabled = settings.enableSwipeActions,
                        doubleTapActionEnabled = settings.enableDoubleTapAction,
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        fullHeightImages = settings.fullHeightImages,
                        fullWidthImages = settings.fullWidthImages,
                        actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments,
                        actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments,
                        showScores = settings.showScores,
                        enableButtonsToScrollBetweenComments = settings.enableButtonsToScrollBetweenComments,
                        commentBarThickness = settings.commentBarThickness,
                        commentIndentAmount = settings.commentIndentAmount,
                    )
                }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.PostRemoved::class).onEach { _ ->
                emitEffect(PostDetailMviModel.Effect.Close)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CommentRemoved::class)
                .onEach { evt ->
                    handleCommentDelete(evt.model.id)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeCommentSortType::class)
                .onEach { evt ->
                    applySortType(evt.value)
                }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.CommentCreated::class).onEach {
                refresh()
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                if (evt.model.id == uiState.value.post.id) {
                    refreshPost()
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Share::class).onEach { evt ->
                shareHelper.share(evt.url)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CopyText::class).onEach {
                emitEffect(PostDetailMviModel.Effect.TriggerCopy(it.value))
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.UserBannedComment::class)
                .onEach { evt ->
                    val commentId = evt.commentId
                    val newUser = evt.user
                    updateState {
                        it.copy(
                            comments =
                                it.comments.map { c ->
                                    if (c.id == commentId) {
                                        c.copy(
                                            creator = newUser,
                                            updateDate = newUser.updateDate,
                                        )
                                    } else {
                                        c
                                    }
                                },
                        )
                    }
                }.launchIn(this)

            searchEventChannel.receiveAsFlow().debounce(1_000).onEach {
                updateState { it.copy(loading = false) }
                emitEffect(PostDetailMviModel.Effect.BackToTop)
                refresh()
            }.launchIn(this)

            identityRepository.isLogged.onEach { logged ->
                updateState { it.copy(isLogged = logged ?: false) }
            }.launchIn(this)

            postNavigationManager.canNavigate.onEach { canNavigate ->
                updateState { it.copy(isNavigationSupported = canNavigate) }
            }.launchIn(this)

            val auth = identityRepository.authToken.value
            val updatedPost =
                postRepository.get(
                    id = postId,
                    auth = auth,
                    instance = otherInstance,
                )
            if (updatedPost != null) {
                updateState {
                    it.copy(post = updatedPost)
                }
                // reset unread comments
                notificationCenter.send(
                    event = NotificationCenterEvent.PostUpdated(updatedPost.copy(unreadComments = 0)),
                )
            }

            if (highlightCommentId != null) {
                val comment =
                    commentRepository.getBy(
                        id = highlightCommentId,
                        auth = auth,
                        instance = otherInstance,
                    )
                highlightCommentPath = comment?.path
            }
            if (isModerator) {
                uiState.value.post.community?.id?.also { communityId ->
                    val moderators =
                        communityRepository.getModerators(
                            auth = auth,
                            id = communityId,
                        )
                    updateState {
                        it.copy(moderators = moderators)
                    }
                }
            }
            if (uiState.value.post.text.isEmpty() && uiState.value.post.title.isEmpty()) {
                refreshPost()
            }
            if (uiState.value.initial) {
                val sortTypes =
                    getSortTypesUseCase.getTypesForComments(otherInstance = otherInstance)
                val defaultCommentSortType =
                    settingsRepository.currentSettings.value.defaultCommentSortType.toSortType()
                updateState {
                    it.copy(
                        sortType = defaultCommentSortType,
                        availableSortTypes = sortTypes,
                    )
                }
                refresh(initial = true)
            }
        }
    }

    private suspend fun downloadUntilHighlight() {
        val highlightPath = highlightCommentPath ?: return
        val indexOfHighlight =
            uiState.value.comments.indexOfFirst {
                it.path == highlightPath
            }
        if (indexOfHighlight == -1) {
            val lastCommentOfThread =
                uiState.value.comments.filter {
                    highlightPath.startsWith(it.path)
                }.takeIf { it.isNotEmpty() }?.maxBy { it.depth }
            if (lastCommentOfThread != null) {
                // comment has an ancestor in the list, go down that path
                loadMoreComments(
                    parentId = lastCommentOfThread.id,
                    loadUntilHighlight = true,
                )
            } else {
                // no ancestor of the comment on this pages, check the next one
                loadNextPage()
            }
        } else {
            // comment to highlight found
            commentWasHighlighted = true
            screenModelScope.launch(Dispatchers.Main) {
                emitEffect(PostDetailMviModel.Effect.ScrollToComment(indexOfHighlight))
            }
        }
    }

    override fun reduce(intent: PostDetailMviModel.Intent) {
        when (intent) {
            PostDetailMviModel.Intent.LoadNextPage ->
                screenModelScope.launch {
                    if (!uiState.value.initial) {
                        loadNextPage()
                    }
                }

            PostDetailMviModel.Intent.Refresh ->
                screenModelScope.launch {
                    refresh()
                }

            PostDetailMviModel.Intent.RefreshPost -> refreshPost()
            PostDetailMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()

            is PostDetailMviModel.Intent.DownVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleDownVoteComment(comment = comment)
                    }
            }

            is PostDetailMviModel.Intent.DownVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleDownVotePost(post = uiState.value.post)
            }

            is PostDetailMviModel.Intent.SaveComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleSaveComment(comment = comment)
                    }
            }

            is PostDetailMviModel.Intent.SavePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleSavePost(post = intent.post)
            }

            is PostDetailMviModel.Intent.Share -> {
                shareHelper.share(intent.url)
            }

            is PostDetailMviModel.Intent.UpVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleUpVoteComment(comment = comment)
                    }
            }

            is PostDetailMviModel.Intent.UpVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleUpVotePost(post = uiState.value.post)
            }

            is PostDetailMviModel.Intent.FetchMoreComments -> {
                loadMoreComments(intent.parentId)
            }

            is PostDetailMviModel.Intent.DeleteComment -> deleteComment(intent.commentId)
            PostDetailMviModel.Intent.DeletePost -> deletePost()

            is PostDetailMviModel.Intent.ToggleExpandComment -> {
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleExpanded(comment)
                    }
            }

            PostDetailMviModel.Intent.ModFeaturePost -> feature(uiState.value.post)
            PostDetailMviModel.Intent.AdminFeaturePost -> featureLocal(uiState.value.post)
            PostDetailMviModel.Intent.ModLockPost -> lock(uiState.value.post)
            is PostDetailMviModel.Intent.ModDistinguishComment ->
                uiState.value.comments.firstOrNull {
                    it.id == intent.commentId
                }?.also { comment ->
                    distinguish(comment)
                }

            is PostDetailMviModel.Intent.ModToggleModUser -> toggleModeratorStatus(intent.id)
            is PostDetailMviModel.Intent.Copy ->
                screenModelScope.launch {
                    emitEffect(PostDetailMviModel.Effect.TriggerCopy(intent.value))
                }

            is PostDetailMviModel.Intent.ChangeSearching -> {
                screenModelScope.launch {
                    updateState { it.copy(searching = intent.value) }
                    if (!intent.value) {
                        updateSearchText("")
                    }
                }
            }

            is PostDetailMviModel.Intent.SetSearch -> updateSearchText(intent.value)
            PostDetailMviModel.Intent.NavigatePrevious -> navigateToPreviousPost()
            PostDetailMviModel.Intent.NavigateNext -> navigateToNextPost()
            is PostDetailMviModel.Intent.NavigatePreviousComment ->
                navigateToPreviousComment(intent.currentIndex)

            is PostDetailMviModel.Intent.NavigateNextComment ->
                navigateToNextComment(intent.currentIndex)
        }
    }

    private fun refreshPost() {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value
            val post = uiState.value.post
            val updatedPost =
                postRepository.get(
                    id = post.id,
                    auth = auth,
                    instance = otherInstance,
                ) ?: post
            updateState {
                it.copy(post = updatedPost)
            }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        commentPaginationManager.reset(
            CommentPaginationSpecification.Replies(
                postId = uiState.value.post.id,
                sortType = uiState.value.sortType,
                otherInstance = otherInstance,
            ),
        )
        updateState {
            it.copy(
                initial = initial,
                canFetchMore = true,
                refreshing = !initial,
                loading = false,
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
        val autoExpandComments = settingsRepository.currentSettings.value.autoExpandComments

        updateState { it.copy(loading = true) }
        val itemList =
            commentPaginationManager.loadNextPage()
                .sortToNestedOrder()
                .populateLoadMoreComments()
                .map {
                    it.copy(
                        expanded = autoExpandComments,
                        // only first level are visible and can be expanded
                        visible = autoExpandComments || it.depth == 0,
                    )
                }

        val comments =
            itemList.let {
                if (currentState.searching) {
                    it.filter { comment ->
                        comment.text.orEmpty()
                            .contains(other = currentState.searchText, ignoreCase = true)
                    }
                } else {
                    it
                }
            }
        updateState {
            it.copy(
                comments = comments,
                loading = false,
                canFetchMore = commentPaginationManager.canFetchMore,
                refreshing = false,
                initial = false,
            )
        }
        if (highlightCommentPath != null && !commentWasHighlighted) {
            downloadUntilHighlight()
        }
    }

    private fun applySortType(value: SortType) {
        if (uiState.value.sortType == value) {
            return
        }
        screenModelScope.launch {
            updateState { it.copy(sortType = value) }
            emitEffect(PostDetailMviModel.Effect.BackToTop)
            delay(50)
            refresh()
        }
    }

    private fun handlePostUpdate(post: PostModel) {
        screenModelScope.launch {
            updateState {
                it.copy(post = post)
            }
        }
    }

    private fun loadMoreComments(
        parentId: Long,
        loadUntilHighlight: Boolean = false,
    ) {
        screenModelScope.launch {
            val currentState = uiState.value
            val auth = identityRepository.authToken.value
            val sort = currentState.sortType
            val fetchResult =
                commentRepository.getChildren(
                    auth = auth,
                    parentId = parentId,
                    instance = otherInstance,
                    sort = sort,
                )
                    ?.sortToNestedOrder(
                        ancestorId = parentId,
                    )
                    ?.filter { c1 ->
                        // prevents accidental duplication
                        currentState.comments.none { c2 -> c2.id == c1.id }
                    }

            val commentsToInsert = fetchResult.orEmpty()
            if (commentsToInsert.isEmpty()) {
                // abort and disable load more button
                val newList =
                    uiState.value.comments.map { comment ->
                        if (comment.id == parentId) {
                            comment.copy(loadMoreButtonVisible = false)
                        } else {
                            comment
                        }
                    }
                updateState { it.copy(comments = newList) }
            } else {
                val newList =
                    uiState.value.comments.let { list ->
                        val index = list.indexOfFirst { c -> c.id == parentId }
                        list.toMutableList().apply {
                            addAll(index + 1, fetchResult.orEmpty())
                        }.toList()
                    }.populateLoadMoreComments()
                updateState { it.copy(comments = newList) }

                if (loadUntilHighlight) {
                    // start indirect recursion
                    downloadUntilHighlight()
                }
            }
        }
    }

    private fun toggleUpVotePost(post: PostModel) {
        val newValue = post.myVote <= 0
        val newPost =
            postRepository.asUpVoted(
                post = post,
                voted = newValue,
            )
        screenModelScope.launch {
            updateState { it.copy(post = newPost) }
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.upVote(
                    auth = auth,
                    post = post,
                    voted = newValue,
                )
                notificationCenter.send(
                    event = NotificationCenterEvent.PostUpdated(newPost),
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                updateState { it.copy(post = post) }
            }
        }
    }

    private fun toggleDownVotePost(post: PostModel) {
        val newValue = post.myVote >= 0
        val newPost =
            postRepository.asDownVoted(
                post = post,
                downVoted = newValue,
            )
        screenModelScope.launch {
            updateState {
                it.copy(post = newPost)
            }
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.downVote(
                    auth = auth,
                    post = post,
                    downVoted = newValue,
                )
                notificationCenter.send(
                    event = NotificationCenterEvent.PostUpdated(newPost),
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                updateState { it.copy(post = post) }
            }
        }
    }

    private fun toggleSavePost(post: PostModel) {
        val newValue = !post.saved
        val newPost =
            postRepository.asSaved(
                post = post,
                saved = newValue,
            )
        screenModelScope.launch {
            updateState { it.copy(post = newPost) }
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postRepository.save(
                    auth = auth,
                    post = post,
                    saved = newValue,
                )
                notificationCenter.send(
                    event = NotificationCenterEvent.PostUpdated(newPost),
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                updateState { it.copy(post = post) }
            }
        }
    }

    private fun handleCommentUpdate(comment: CommentModel) {
        screenModelScope.launch {
            updateState {
                it.copy(
                    comments =
                        it.comments.map { c ->
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

    private fun toggleUpVoteComment(comment: CommentModel) {
        val newValue = comment.myVote <= 0
        val newComment =
            commentRepository.asUpVoted(
                comment = comment,
                voted = newValue,
            )
        handleCommentUpdate(newComment)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.upVote(
                    auth = auth,
                    comment = comment,
                    voted = newValue,
                )
                notificationCenter.send(
                    event = NotificationCenterEvent.CommentUpdated(newComment),
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
            }
        }
    }

    private fun toggleDownVoteComment(comment: CommentModel) {
        val newValue = comment.myVote >= 0
        val newComment = commentRepository.asDownVoted(comment, newValue)
        handleCommentUpdate(newComment)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.downVote(
                    auth = auth,
                    comment = comment,
                    downVoted = newValue,
                )
                notificationCenter.send(
                    event = NotificationCenterEvent.CommentUpdated(newComment),
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
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
        handleCommentUpdate(newComment)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.save(
                    auth = auth,
                    comment = comment,
                    saved = newValue,
                )
                notificationCenter.send(
                    event = NotificationCenterEvent.CommentUpdated(newComment),
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
            }
        }
    }

    private fun deleteComment(id: Long) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            commentRepository.delete(id, auth)
            handleCommentDelete(id)
            refreshPost()
        }
    }

    private fun handleCommentDelete(id: Long) {
        screenModelScope.launch {
            updateState { it.copy(comments = it.comments.filter { comment -> comment.id != id }) }
        }
    }

    private fun deletePost() {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            val postId = uiState.value.post.id
            postRepository.delete(id = postId, auth = auth)
            notificationCenter.send(
                event = NotificationCenterEvent.PostDeleted(uiState.value.post),
            )
            emitEffect(PostDetailMviModel.Effect.Close)
        }
    }

    private fun toggleExpanded(comment: CommentModel) {
        screenModelScope.launch(Dispatchers.Main) {
            val commentId = comment.id
            val newExpanded = !comment.expanded
            updateState {
                val newComments =
                    it.comments.map { c ->
                        when {
                            c.id == commentId -> {
                                // change expanded state of comment itself
                                c.copy(expanded = newExpanded)
                            }

                            c.path.contains(".$commentId.") && c.depth > comment.depth -> {
                                // if expanded, make all children visible and expanded
                                // otherwise, make all children not visible (doesn't matter expanded)
                                if (newExpanded) {
                                    c.copy(visible = true, expanded = true)
                                } else {
                                    c.copy(visible = false, expanded = true)
                                }
                            }

                            else -> {
                                c
                            }
                        }
                    }
                it.copy(comments = newComments)
            }
        }
    }

    private fun feature(post: PostModel) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            val newPost =
                postRepository.featureInCommunity(
                    postId = post.id,
                    auth = auth,
                    featured = !post.featuredCommunity,
                )
            if (newPost != null) {
                handlePostUpdate(newPost)
            }
        }
    }

    private fun featureLocal(post: PostModel) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            val newPost =
                postRepository.featureInInstance(
                    postId = post.id,
                    auth = auth,
                    featured = !post.featuredLocal,
                )
            if (newPost != null) {
                handlePostUpdate(newPost)
            }
        }
    }

    private fun lock(post: PostModel) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            val newPost =
                postRepository.lock(
                    postId = post.id,
                    auth = auth,
                    locked = !post.locked,
                )
            if (newPost != null) {
                handlePostUpdate(newPost)
            }
        }
    }

    private fun distinguish(comment: CommentModel) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            val newComment =
                commentRepository.distinguish(
                    commentId = comment.id,
                    auth = auth,
                    distinguished = !comment.distinguished,
                )
            if (newComment != null) {
                handleCommentUpdate(newComment)
            }
        }
    }

    private fun toggleModeratorStatus(userId: Long) {
        screenModelScope.launch {
            val isModerator = uiState.value.moderators.containsId(userId)
            val auth = identityRepository.authToken.value.orEmpty()
            val post = uiState.value.post
            val communityId = post.community?.id
            if (communityId != null) {
                val newModerators =
                    communityRepository.addModerator(
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
    }

    private fun updateSearchText(value: String) {
        screenModelScope.launch {
            updateState { it.copy(searchText = value) }
            searchEventChannel.send(Unit)
        }
    }

    private fun navigateToPreviousPost() {
        val currentId = uiState.value.post.id
        screenModelScope.launch {
            updateState { it.copy(loading = true, initial = true) }
            postNavigationManager.getPrevious(currentId)?.also { newPost ->
                loadNewPost(newPost)
            }
        }
    }

    private fun navigateToNextPost() {
        val currentId = uiState.value.post.id
        screenModelScope.launch {
            updateState { it.copy(loading = true, initial = true) }
            postNavigationManager.getNext(currentId)?.also { newPost ->
                loadNewPost(newPost)
            }
        }
    }

    private suspend fun loadNewPost(post: PostModel) {
        itemCache.putPost(post)
        updateState {
            it.copy(
                searching = false,
                searchText = "",
                post = post,
            )
        }
        // reset unread comments
        notificationCenter.send(
            event = NotificationCenterEvent.PostUpdated(post.copy(unreadComments = 0)),
        )
        emitEffect(PostDetailMviModel.Effect.BackToTop)
        refresh()
    }

    private fun navigateToPreviousComment(index: Int) {
        val comments = uiState.value.comments.takeIf { it.isNotEmpty() } ?: return
        val (start, end) = 0 to index.coerceAtMost(comments.lastIndex)
        val newIndex =
            comments.subList(
                fromIndex = start,
                toIndex = end,
            ).indexOfLast {
                it.depth == 0
            }.takeIf { it >= 0 }
        if (newIndex != null) {
            screenModelScope.launch {
                emitEffect(PostDetailMviModel.Effect.ScrollToComment(newIndex))
            }
        }
    }

    private fun navigateToNextComment(index: Int) {
        val comments = uiState.value.comments.takeIf { it.isNotEmpty() } ?: return
        val (start, end) = (index + 1).coerceAtMost(comments.lastIndex) to comments.lastIndex
        val newIndex =
            comments.subList(
                fromIndex = start,
                toIndex = end,
            ).indexOfFirst {
                it.depth == 0
            }.takeIf { it >= 0 }?.let {
                it + start
            }
        if (newIndex != null) {
            screenModelScope.launch {
                emitEffect(PostDetailMviModel.Effect.ScrollToComment(newIndex))
            }
        } else if (uiState.value.canFetchMore) {
            // fetch a new page and try again if possible (terminates on pagination end)
            screenModelScope.launch {
                loadNextPage()
                navigateToNextComment(index)
            }
        }
    }
}
