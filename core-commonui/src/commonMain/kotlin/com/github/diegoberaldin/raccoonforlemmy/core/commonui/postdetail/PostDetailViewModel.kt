package com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.containsId
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val mvi: DefaultMviModel<PostDetailMviModel.Intent, PostDetailMviModel.UiState, PostDetailMviModel.Effect>,
    private val post: PostModel,
    private val otherInstance: String,
    private val highlightCommentId: Int?,
    private val isModerator: Boolean,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val communityRepository: CommunityRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val shareHelper: ShareHelper,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
) : PostDetailMviModel,
    MviModel<PostDetailMviModel.Intent, PostDetailMviModel.UiState, PostDetailMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    private var highlightCommentPath: String? = null
    private var commentWasHighlighted = false

    override fun onStarted() {
        mvi.onStarted()

        mvi.scope?.launch(Dispatchers.Main) {
            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.UserBannedComment::class)
                .onEach { evt ->
                    val commentId = evt.commentId
                    val newUser = evt.user
                    mvi.updateState {
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
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)

            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        swipeActionsEnabled = settings.enableSwipeActions,
                        doubleTapActionEnabled = settings.enableDoubleTapAction,
                        sortType = settings.defaultCommentSortType.toSortType(),
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        fullHeightImages = settings.fullHeightImages,
                    )
                }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.PostRemoved::class).onEach { evt ->
                mvi.emitEffect(PostDetailMviModel.Effect.Close)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CommentRemoved::class)
                .onEach { evt ->
                    handleCommentDelete(evt.model.id)
                }.launchIn(this)
        }

        mvi.scope?.launch(Dispatchers.IO) {
            identityRepository.isLogged.onEach { logged ->
                mvi.updateState { it.copy(isLogged = logged ?: false) }
            }.launchIn(this)
            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                mvi.updateState {
                    it.copy(
                        currentUserId = user?.id ?: 0,
                    )
                }
            }

            mvi.updateState {
                it.copy(
                    post = post,
                    isModerator = isModerator,
                )
            }

            val auth = identityRepository.authToken.value
            val updatedPost = postRepository.get(
                id = post.id,
                auth = auth,
                instance = otherInstance,
            )
            if (updatedPost != null) {
                mvi.updateState {
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
                post.community?.id?.also { communityId ->
                    val moderators = communityRepository.getModerators(
                        auth = auth,
                        id = communityId
                    )
                    mvi.updateState {
                        it.copy(moderators = moderators)
                    }
                }

            }
            if (post.text.isEmpty() && post.title.isEmpty()) {
                refreshPost()
            }
            if (mvi.uiState.value.comments.isEmpty()) {
                refresh()
            }
        }
    }

    private fun downloadUntilHighlight() {
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
            mvi.scope?.launch {
                mvi.emitEffect(PostDetailMviModel.Effect.ScrollToComment(indexOfHighlight))
            }
        }
    }

    override fun reduce(intent: PostDetailMviModel.Intent) {
        when (intent) {
            PostDetailMviModel.Intent.LoadNextPage -> {
                if (!uiState.value.initial) {
                    loadNextPage()
                }
            }

            PostDetailMviModel.Intent.Refresh -> refresh()
            PostDetailMviModel.Intent.RefreshPost -> refreshPost()
            PostDetailMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is PostDetailMviModel.Intent.ChangeSort -> applySortType(intent.value)

            is PostDetailMviModel.Intent.DownVoteComment -> {
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleDownVoteComment(
                            comment = comment,
                            feedback = intent.feedback,
                        )
                    }
            }

            is PostDetailMviModel.Intent.DownVotePost -> toggleDownVotePost(
                post = uiState.value.post,
                feedback = intent.feedback,
            )

            is PostDetailMviModel.Intent.SaveComment -> {
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleSaveComment(
                            comment = comment,
                            feedback = intent.feedback,
                        )
                    }
            }

            is PostDetailMviModel.Intent.SavePost -> toggleSavePost(
                post = intent.post,
                feedback = intent.feedback,
            )

            is PostDetailMviModel.Intent.UpVoteComment -> {
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleUpVoteComment(
                            comment = comment,
                            feedback = intent.feedback,
                        )
                    }
            }

            is PostDetailMviModel.Intent.UpVotePost -> toggleUpVotePost(
                post = uiState.value.post,
                feedback = intent.feedback,
            )

            is PostDetailMviModel.Intent.FetchMoreComments -> {
                loadMoreComments(intent.parentId)
            }

            is PostDetailMviModel.Intent.DeleteComment -> deleteComment(intent.commentId)
            PostDetailMviModel.Intent.DeletePost -> deletePost()
            PostDetailMviModel.Intent.SharePost -> share(
                post = uiState.value.post,
            )

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
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            val updatedPost = postRepository.get(
                id = post.id,
                auth = auth,
                instance = otherInstance,
            ) ?: post
            mvi.updateState {
                it.copy(post = updatedPost)
            }
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
        val autoExpandComments = settingsRepository.currentSettings.value.autoExpandComments

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val refreshing = currentState.refreshing
            val sort = currentState.sortType
            val itemList = commentRepository.getAll(
                auth = auth,
                postId = post.id,
                instance = otherInstance,
                page = currentPage,
                sort = sort,
            )?.processCommentsToGetNestedOrder(
                ancestorId = null,
            )?.populateLoadMoreComments()?.let { list ->
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
            mvi.updateState {
                val newcomments = if (refreshing) {
                    itemList.orEmpty()
                } else {
                    it.comments + itemList.orEmpty()
                }
                it.copy(
                    comments = newcomments,
                    loading = false,
                    canFetchMore = itemList?.isEmpty() != true,
                    refreshing = false,
                    initial = false,
                )

            }
            if (highlightCommentPath != null && !commentWasHighlighted) {
                downloadUntilHighlight()
            }
        }
    }

    private fun applySortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        mvi.scope?.launch {
            mvi.emitEffect(PostDetailMviModel.Effect.BackToTop)
        }
        refresh()
    }

    private fun handlePostUpdate(post: PostModel) {
        mvi.updateState {
            it.copy(post = post)
        }
    }

    private fun loadMoreComments(parentId: Int, loadUntilHighlight: Boolean = false) {
        mvi.scope?.launch(Dispatchers.IO) {
            val currentState = mvi.uiState.value
            val auth = identityRepository.authToken.value
            val sort = currentState.sortType
            val fetchResult = commentRepository.getChildren(
                auth = auth,
                parentId = parentId,
                instance = otherInstance,
                sort = sort,
            )?.processCommentsToGetNestedOrder(
                ancestorId = parentId.toString(),
            )?.filter {
                currentState.comments.none { c -> c.id == it.id }
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
                mvi.updateState { it.copy(comments = newList) }
            } else {
                val newList = uiState.value.comments.let { list ->
                    val index = list.indexOfFirst { c -> c.id == parentId }
                    list.toMutableList().apply {
                        addAll(index + 1, fetchResult.orEmpty())
                    }.toList()
                }.populateLoadMoreComments()
                mvi.updateState { it.copy(comments = newList) }

                if (loadUntilHighlight) {
                    // start indirect recursion
                    downloadUntilHighlight()
                }
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
        mvi.updateState { it.copy(post = newPost) }
        mvi.scope?.launch(Dispatchers.IO) {
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
                mvi.updateState { it.copy(post = post) }
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
        mvi.updateState { it.copy(post = newPost) }
        mvi.scope?.launch(Dispatchers.IO) {
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
                mvi.updateState { it.copy(post = post) }
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
        mvi.updateState { it.copy(post = newPost) }
        mvi.scope?.launch(Dispatchers.IO) {
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
                mvi.updateState { it.copy(post = post) }
            }
        }
    }

    private fun handleCommentUpdate(comment: CommentModel) {
        mvi.updateState {
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
        handleCommentUpdate(newComment)
        mvi.scope?.launch(Dispatchers.IO) {
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

    private fun toggleDownVoteComment(
        comment: CommentModel,
        feedback: Boolean,
    ) {
        val newValue = comment.myVote >= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newComment = commentRepository.asDownVoted(comment, newValue)
        handleCommentUpdate(newComment)
        mvi.scope?.launch(Dispatchers.IO) {
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
        handleCommentUpdate(newComment)
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            commentRepository.delete(id, auth)
            handleCommentDelete(id)
            refreshPost()
        }
    }

    private fun handleCommentDelete(id: Int) {
        mvi.updateState { it.copy(comments = it.comments.filter { comment -> comment.id != id }) }
    }

    private fun deletePost() {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            postRepository.delete(id = post.id, auth = auth)
            notificationCenter.send(
                event = NotificationCenterEvent.PostDeleted(post),
            )
            mvi.emitEffect(PostDetailMviModel.Effect.Close)
        }
    }

    private fun share(post: PostModel) {
        val url = post.originalUrl.orEmpty()
        if (url.isNotEmpty()) {
            shareHelper.share(url, "text/plain")
        }
    }

    private fun toggleExpanded(comment: CommentModel) {
        mvi.scope?.launch {
            val commentId = comment.id
            val newExpanded = !comment.expanded
            mvi.updateState {
                val newComments = it.comments.map { c ->
                    when {
                        c.id == commentId -> {
                            // change expanded state of comment itself
                            c.copy(expanded = newExpanded)
                        }

                        c.path.contains(".$commentId.") && c.depth > comment.depth -> {
                            // if expanded, make all childern visible and expanded
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.scope?.launch(Dispatchers.IO) {
            val isModerator = uiState.value.moderators.containsId(userId)
            val auth = identityRepository.authToken.value.orEmpty()
            val communityId = post.community?.id
            if (communityId != null) {
                val newModerators = communityRepository.addModerator(
                    auth = auth,
                    communityId = communityId,
                    added = !isModerator,
                    userId = userId,
                )
                mvi.updateState {
                    it.copy(moderators = newModerators)
                }
            }
        }
    }
}

private data class Node(
    val comment: CommentModel?,
    val children: MutableList<Node> = mutableListOf(),
)

private fun findNode(id: String, node: Node): Node? {
    if (node.comment?.id.toString() == id) {
        return node
    }
    for (c in node.children) {
        val res = findNode(id, c)
        if (res != null) {
            return res
        }
    }
    return null
}


private fun linearize(node: Node, list: MutableList<CommentModel>) {
    if (node.comment != null) {
        list.add(node.comment)
    }
    for (c in node.children) {
        linearize(c, list)
    }
}

private fun List<CommentModel>.populateLoadMoreComments() = mapIndexed { idx, comment ->
    val hasMoreComments = (comment.comments ?: 0) > 0
    val isNextCommentNotChild =
        idx < lastIndex && this[idx + 1].depth <= comment.depth
    comment.copy(loadMoreButtonVisible = hasMoreComments && isNextCommentNotChild)
}

private fun List<CommentModel>.processCommentsToGetNestedOrder(
    ancestorId: String? = null,
): List<CommentModel> {
    val root = Node(null)
    // reconstructs the tree
    for (c in this) {
        val parentId = c.parentId
        if (parentId == ancestorId) {
            root.children += Node(c)
        } else if (parentId != null) {
            val parent = findNode(parentId, root)
            if (parent != null) {
                parent.children += Node(c)
            }
        }
    }

    // linearize the tree depth first
    val result = mutableListOf<CommentModel>()
    linearize(root, result)

    return result.toList()
}
