package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail

import cafe.adriel.voyager.core.model.screenModelScope
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val postId: Int,
    private val otherInstance: String,
    private val highlightCommentId: Int?,
    private val isModerator: Boolean,
    private val identityRepository: IdentityRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val siteRepository: SiteRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val communityRepository: CommunityRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val shareHelper: ShareHelper,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
    private val getSortTypesUseCase: GetSortTypesUseCase,
    private val itemCache: LemmyItemCache,
) : PostDetailMviModel,
    DefaultMviModel<PostDetailMviModel.Intent, PostDetailMviModel.UiState, PostDetailMviModel.Effect>(
        initialState = PostDetailMviModel.UiState()
    ) {

    private var currentPage: Int = 1
    private var highlightCommentPath: String? = null
    private var commentWasHighlighted = false

    init {
        updateState {
            it.copy(
                instance = otherInstance.takeIf { n -> n.isNotEmpty() }
                    ?: apiConfigurationRepository.instance.value,
            )
        }
        screenModelScope.launch {
            if (uiState.value.post.id == 0) {
                val post = itemCache.getPost(postId) ?: PostModel()
                updateState {
                    it.copy(
                        post = post,
                        isModerator = isModerator,
                    )
                }
            }
            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.UserBannedComment::class)
                .onEach { evt ->
                    val commentId = evt.commentId
                    val newUser = evt.user
                    updateState {
                        it.copy(
                            comments = it.comments.map { c ->
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
                        actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments,
                        actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments,
                        showScores = settings.showScores,
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

            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach {
                refreshPost()
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Share::class).onEach { evt ->
                shareHelper.share(evt.url)
            }.launchIn(this)

            identityRepository.isLogged.onEach { logged ->
                updateState { it.copy(isLogged = logged ?: false) }
            }.launchIn(this)

            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                updateState {
                    it.copy(currentUserId = user?.id ?: 0)
                }
            }

            val auth = identityRepository.authToken.value
            val updatedPost = postRepository.get(
                id = postId,
                auth = auth,
                instance = otherInstance,
            )
            if (updatedPost != null) {
                updateState {
                    it.copy(post = updatedPost)
                }
            }

            if (highlightCommentId != null) {
                val comment = commentRepository.getBy(
                    id = highlightCommentId,
                    auth = auth,
                    instance = otherInstance,
                )
                highlightCommentPath = comment?.path
            }
            if (isModerator) {
                uiState.value.post.community?.id?.also { communityId ->
                    val moderators = communityRepository.getModerators(
                        auth = auth,
                        id = communityId
                    )
                    updateState {
                        it.copy(moderators = moderators)
                    }
                }

            }
            if (uiState.value.post.text.isEmpty() && uiState.value.post.title.isEmpty()) {
                refreshPost()
            }
            if (uiState.value.comments.isEmpty()) {
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
                refresh()
            }
        }
    }

    private suspend fun downloadUntilHighlight() {
        val highlightPath = highlightCommentPath ?: return

        val indexOfHighlight = uiState.value.comments.indexOfFirst {
            it.path == highlightPath
        }
        if (indexOfHighlight == -1) {
            val lastCommentOfThread = uiState.value.comments.filter {
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
            PostDetailMviModel.Intent.LoadNextPage -> screenModelScope.launch {
                if (!uiState.value.initial) {
                    loadNextPage()
                }
            }

            PostDetailMviModel.Intent.Refresh -> screenModelScope.launch {
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
            PostDetailMviModel.Intent.ModLockPost -> lock(uiState.value.post)
            is PostDetailMviModel.Intent.ModDistinguishComment -> uiState.value.comments.firstOrNull { it.id == intent.commentId }
                ?.also { comment ->
                    distinguish(comment)
                }

            is PostDetailMviModel.Intent.ModToggleModUser -> toggleModeratorStatus(intent.id)
        }
    }

    private fun refreshPost() {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value
            val updatedPost = postRepository.get(
                id = postId,
                auth = auth,
                instance = otherInstance,
            ) ?: uiState.value.post
            updateState {
                it.copy(post = updatedPost)
            }
        }
    }

    private suspend fun refresh() {
        currentPage = 1
        updateState { it.copy(canFetchMore = true, refreshing = true) }
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
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val sort = currentState.sortType
        val itemList = commentRepository.getAll(
            auth = auth,
            postId = postId,
            instance = otherInstance,
            page = currentPage,
            sort = sort,
        )
            ?.sortToNestedOrder()
            ?.populateLoadMoreComments()
            ?.let { list ->
                if (refreshing) {
                    list
                } else {
                    list.filter { c1 ->
                        // prevents accidental duplication
                        currentState.comments.none { c2 -> c1.id == c2.id }
                    }
                }
            }?.map {
                it.copy(
                    expanded = autoExpandComments,
                    // only first level are visible and can be expanded
                    visible = autoExpandComments || it.depth == 0
                )
            }

        if (!itemList.isNullOrEmpty()) {
            currentPage++
        }
        val itemsToAdd = itemList.orEmpty()
            .filterNot {
                it.deleted
            }
        updateState {
            val newComments = if (refreshing) {
                itemsToAdd
            } else {
                it.comments + itemsToAdd
            }
            it.copy(
                comments = newComments,
                loading = false,
                // deleted commments should not be counted
                canFetchMore = itemsToAdd.isNotEmpty(),
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
        updateState { it.copy(sortType = value) }
        screenModelScope.launch {
            emitEffect(PostDetailMviModel.Effect.BackToTop)
            refresh()
        }
    }

    private fun handlePostUpdate(post: PostModel) {
        updateState {
            it.copy(post = post)
        }
    }

    private fun loadMoreComments(parentId: Int, loadUntilHighlight: Boolean = false) {
        screenModelScope.launch {
            val currentState = uiState.value
            val auth = identityRepository.authToken.value
            val sort = currentState.sortType
            val fetchResult = commentRepository.getChildren(
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
                val newList = uiState.value.comments.map { comment ->
                    if (comment.id == parentId) {
                        comment.copy(loadMoreButtonVisible = false)
                    } else {
                        comment
                    }
                }
                updateState { it.copy(comments = newList) }
            } else {
                val newList = uiState.value.comments.let { list ->
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
        val newPost = postRepository.asUpVoted(
            post = post,
            voted = newValue,
        )
        updateState { it.copy(post = newPost) }
        screenModelScope.launch {
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

    private fun toggleDownVotePost(
        post: PostModel,
    ) {
        val newValue = post.myVote >= 0
        val newPost = postRepository.asDownVoted(
            post = post,
            downVoted = newValue,
        )
        updateState {
            it.copy(post = newPost)
        }
        screenModelScope.launch {
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
        val newPost = postRepository.asSaved(
            post = post,
            saved = newValue,
        )
        updateState { it.copy(post = newPost) }
        screenModelScope.launch {
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
        updateState {
            it.copy(
                comments = it.comments.map { c ->
                    if (c.id == comment.id) {
                        comment
                    } else {
                        c
                    }
                },
            )
        }
    }

    private fun toggleUpVoteComment(comment: CommentModel) {
        val newValue = comment.myVote <= 0
        val newComment = commentRepository.asUpVoted(
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
        val newComment = commentRepository.asSaved(
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

    private fun deleteComment(id: Int) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            commentRepository.delete(id, auth)
            handleCommentDelete(id)
            refreshPost()
        }
    }

    private fun handleCommentDelete(id: Int) {
        updateState { it.copy(comments = it.comments.filter { comment -> comment.id != id }) }
    }

    private fun deletePost() {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
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
                val newComments = it.comments.map { c ->
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
        screenModelScope.launch {
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

    private fun distinguish(comment: CommentModel) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            val newComment = commentRepository.distinguish(
                commentId = comment.id,
                auth = auth,
                distinguished = !comment.distinguished,
            )
            if (newComment != null) {
                handleCommentUpdate(newComment)
            }
        }
    }

    private fun toggleModeratorStatus(userId: Int) {
        screenModelScope.launch {
            val isModerator = uiState.value.moderators.containsId(userId)
            val auth = identityRepository.authToken.value.orEmpty()
            val post = uiState.value.post
            val communityId = post.community?.id
            if (communityId != null) {
                val newModerators = communityRepository.addModerator(
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
}