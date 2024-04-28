package com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents

import cafe.adriel.voyager.core.model.screenModelScope
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommentPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommentPaginationSpecification
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.PostPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.PostPaginationSpecification
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.ImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FilteredContentsViewModel(
    private val contentsType: Int,
    private val postPaginationManager: PostPaginationManager,
    private val commentPaginationManager: CommentPaginationManager,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val identityRepository: IdentityRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val imagePreloadManager: ImagePreloadManager,
    private val hapticFeedback: HapticFeedback,
    private val notificationCenter: NotificationCenter,
) : FilteredContentsMviModel,
    DefaultMviModel<FilteredContentsMviModel.Intent, FilteredContentsMviModel.State, FilteredContentsMviModel.Effect>(
        initialState = FilteredContentsMviModel.State(),
    ) {

    init {
        screenModelScope.launch {
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        contentsType = contentsType.toFilteredContentsType(),
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        voteFormat = settings.voteFormat,
                        fullHeightImages = settings.fullHeightImages,
                        fadeReadPosts = settings.fadeReadPosts,
                        showUnreadComments = settings.showUnreadComments,
                        actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts,
                        actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts,
                        actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments,
                        actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments,
                    )
                }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.ChangedLikedType::class).onEach { evt ->
                changeLiked(evt.value)
            }.launchIn(this)

            if (uiState.value.posts.isEmpty()) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: FilteredContentsMviModel.Intent) {
        when (intent) {
            is FilteredContentsMviModel.Intent.ChangeSection -> changeSection(intent.value)
            FilteredContentsMviModel.Intent.Refresh -> screenModelScope.launch {
                refresh()
            }

            FilteredContentsMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            FilteredContentsMviModel.Intent.LoadNextPage -> screenModelScope.launch {
                loadNextPage()
            }

            is FilteredContentsMviModel.Intent.UpVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleUpVote(post = post)
                }
            }

            is FilteredContentsMviModel.Intent.DownVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleDownVote(post = post)
                }
            }

            is FilteredContentsMviModel.Intent.SavePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleSave(post = post)
                }
            }

            is FilteredContentsMviModel.Intent.ModFeaturePost -> uiState.value.posts.firstOrNull { it.id == intent.id }
                ?.also { post ->
                    feature(post = post)
                }

            is FilteredContentsMviModel.Intent.ModLockPost -> uiState.value.posts.firstOrNull { it.id == intent.id }
                ?.also { post ->
                    lock(post = post)
                }

            is FilteredContentsMviModel.Intent.UpVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.commentId }?.also { comment ->
                    toggleUpVoteComment(comment = comment)
                }
            }

            is FilteredContentsMviModel.Intent.DownVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.commentId }?.also { comment ->
                    toggleDownVoteComment(comment = comment)
                }
            }

            is FilteredContentsMviModel.Intent.SaveComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.commentId }?.also { comment ->
                    toggleSaveComment(comment = comment)
                }
            }

            is FilteredContentsMviModel.Intent.ModDistinguishComment -> uiState.value.comments.firstOrNull {
                it.id == intent.commentId
            }?.also { comment ->
                distinguish(comment)
            }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        val currentState = uiState.value
        val postSpecification = when (currentState.contentsType) {
            FilteredContentsType.Moderated -> PostPaginationSpecification.Listing(
                listingType = ListingType.ModeratorView,
                sortType = SortType.New,
            )

            FilteredContentsType.Votes -> PostPaginationSpecification.Votes(
                liked = currentState.liked,
                sortType = SortType.New,
            )
        }
        postPaginationManager.reset(postSpecification)
        val commentSpecification = when (currentState.contentsType) {
            FilteredContentsType.Moderated -> CommentPaginationSpecification.Replies(
                listingType = ListingType.ModeratorView,
                sortType = SortType.New,
            )

            FilteredContentsType.Votes -> CommentPaginationSpecification.Votes(
                liked = currentState.liked,
                sortType = SortType.New,
            )
        }
        commentPaginationManager.reset(commentSpecification)
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
                loading = false,
            )
        }
        screenModelScope.launch {
            loadNextPage()
        }
    }

    private fun changeSection(section: FilteredContentsSection) {
        updateState {
            it.copy(
                section = section,
            )
        }
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }
        when (currentState.contentsType) {
            FilteredContentsType.Moderated -> loadNextPageModded()
            FilteredContentsType.Votes -> loadNextPageVotes()
        }
    }

    private suspend fun loadNextPageModded() {
        val currentState = uiState.value
        updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value.orEmpty()
        val refreshing = currentState.refreshing

        if (currentState.section == FilteredContentsSection.Posts) {
            coroutineScope {
                val posts = async {
                    postPaginationManager.loadNextPage()
                }.await()
                val comments = async {
                    if (currentState.comments.isEmpty() || refreshing) {
                        // this is needed because otherwise on first selector change
                        // the lazy column scrolls back to top (it must have an empty data set)
                        commentRepository.getAll(
                            auth = auth,
                            page = 1,
                            type = ListingType.ModeratorView,
                        ).orEmpty()
                    } else {
                        currentState.comments
                    }
                }.await()
                if (uiState.value.autoLoadImages) {
                    posts.forEach { post ->
                        post.imageUrl.takeIf { i -> i.isNotEmpty() }?.also { url ->
                            imagePreloadManager.preload(url)
                        }
                    }
                }
                updateState {
                    it.copy(
                        posts = posts,
                        comments = comments,
                        loading = if (it.initial) posts.isEmpty() else false,
                        canFetchMore = postPaginationManager.canFetchMore,
                        refreshing = false,
                        initial = if (it.initial) posts.isEmpty() else false,
                    )
                }
            }
        } else {
            val comments = commentPaginationManager.loadNextPage()
            updateState {
                it.copy(
                    comments = comments,
                    loading = if (it.initial) comments.isEmpty() else false,
                    canFetchMore = commentPaginationManager.canFetchMore,
                    refreshing = false,
                    initial = if (it.initial) comments.isEmpty() else false,
                )
            }
        }
    }

    private suspend fun loadNextPageVotes() {
        val currentState = uiState.value
        updateState { it.copy(loading = true) }
        val refreshing = currentState.refreshing

        if (currentState.section == FilteredContentsSection.Posts) {
            coroutineScope {
                val posts = async {
                    postPaginationManager.loadNextPage()
                }.await()
                val comments = async {
                    if (currentState.comments.isEmpty() || refreshing) {
                        // this is needed because otherwise on first selector change
                        // the lazy column scrolls back to top (it must have an empty data set)
                        commentPaginationManager.loadNextPage()
                    } else {
                        currentState.comments
                    }
                }.await()
                if (uiState.value.autoLoadImages) {
                    posts.forEach { post ->
                        post.imageUrl.takeIf { i -> i.isNotEmpty() }?.also { url ->
                            imagePreloadManager.preload(url)
                        }
                    }
                }
                updateState {
                    it.copy(
                        posts = posts,
                        comments = comments,
                        loading = if (it.initial) posts.isEmpty() else false,
                        canFetchMore = postPaginationManager.canFetchMore,
                        refreshing = false,
                        initial = if (it.initial) posts.isEmpty() else false,
                    )
                }
            }
        } else {
            val comments = commentPaginationManager.loadNextPage()
            updateState {
                it.copy(
                    comments = comments,
                    loading = if (it.initial) comments.isEmpty() else false,
                    canFetchMore = commentPaginationManager.canFetchMore,
                    refreshing = false,
                    initial = if (it.initial) comments.isEmpty() else false,
                )
            }
        }
    }

    private fun toggleUpVote(post: PostModel) {
        val newValue = post.myVote <= 0
        val newPost = postRepository.asUpVoted(
            post = post,
            voted = newValue,
        )
        handlePostUpdate(newPost)
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
                handlePostUpdate(post)
            }
        }
    }

    private fun toggleDownVote(post: PostModel) {
        val newValue = post.myVote >= 0
        val newPost = postRepository.asDownVoted(
            post = post,
            downVoted = newValue,
        )
        handlePostUpdate(newPost)
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
                handlePostUpdate(post)
            }
        }
    }

    private fun toggleSave(post: PostModel) {
        val newValue = !post.saved
        val newPost = postRepository.asSaved(
            post = post,
            saved = newValue,
        )
        handlePostUpdate(newPost)
        screenModelScope.launch(Dispatchers.IO) {
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
                handlePostUpdate(post)
            }
        }
    }

    private fun feature(post: PostModel) {
        screenModelScope.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            val newPost = postRepository.featureInCommunity(
                postId = post.id, auth = auth, featured = !post.featuredCommunity
            )
            if (newPost != null) {
                handlePostUpdate(newPost)
            }
        }
    }

    private fun lock(post: PostModel) {
        screenModelScope.launch(Dispatchers.IO) {
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

    private fun handlePostUpdate(post: PostModel) {
        updateState {
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
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
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

    private fun changeLiked(value: Boolean) {
        updateState { it.copy(liked = value) }
        screenModelScope.launch {
            refresh(initial = true)
            emitEffect(FilteredContentsMviModel.Effect.BackToTop)
        }
    }
}
