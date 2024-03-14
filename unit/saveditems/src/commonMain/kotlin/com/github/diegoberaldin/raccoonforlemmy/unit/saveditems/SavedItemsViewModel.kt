package com.github.diegoberaldin.raccoonforlemmy.unit.saveditems

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
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SavedItemsViewModel(
    private val identityRepository: IdentityRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val siteRepository: SiteRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val shareHelper: ShareHelper,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
    private val getSortTypesUseCase: GetSortTypesUseCase,
) : SavedItemsMviModel,
    DefaultMviModel<SavedItemsMviModel.Intent, SavedItemsMviModel.UiState, SavedItemsMviModel.Effect>(
        initialState = SavedItemsMviModel.UiState(),
    ) {

    private var currentPage: Int = 1

    init {
        updateState { it.copy(instance = apiConfigurationRepository.instance.value) }
        screenModelScope.launch {
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        fullHeightImages = settings.fullHeightImages,
                        showScores = settings.showScores,
                    )
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostUpdated::class).onEach { evt ->
                handlePostUpdate(evt.model)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.PostDeleted::class).onEach { evt ->
                handlePostDelete(evt.model.id)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    if (evt.screenKey == "savedItems") {
                        applySortType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Share::class).onEach { evt ->
                shareHelper.share(evt.url)
            }.launchIn(this)

            if (uiState.value.posts.isEmpty()) {
                val sortTypes = getSortTypesUseCase.getTypesForSavedItems()
                updateState { it.copy(availableSortTypes = sortTypes) }
                refresh()
            }
        }
    }

    override fun reduce(intent: SavedItemsMviModel.Intent) {
        when (intent) {
            SavedItemsMviModel.Intent.LoadNextPage -> loadNextPage()
            SavedItemsMviModel.Intent.Refresh -> refresh()
            is SavedItemsMviModel.Intent.ChangeSection -> changeSection(intent.section)
            is SavedItemsMviModel.Intent.DownVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleDownVoteComment(
                    comment = uiState.value.comments.first { it.id == intent.id },
                )
            }

            is SavedItemsMviModel.Intent.DownVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleDownVotePost(
                    post = uiState.value.posts.first { it.id == intent.id },
                )
            }

            is SavedItemsMviModel.Intent.SaveComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleSaveComment(
                    comment = uiState.value.comments.first { it.id == intent.id },
                )
            }

            is SavedItemsMviModel.Intent.SavePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleSavePost(
                    post = uiState.value.posts.first { it.id == intent.id },
                )
            }

            is SavedItemsMviModel.Intent.Share -> {
                shareHelper.share(intent.url)
            }

            is SavedItemsMviModel.Intent.UpVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleUpVoteComment(
                    comment = uiState.value.comments.first { it.id == intent.id },
                )
            }

            is SavedItemsMviModel.Intent.UpVotePost -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                toggleUpVotePost(
                    post = uiState.value.posts.first { it.id == intent.id },
                )
            }
        }
    }

    private fun refresh() {
        currentPage = 1
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
            )
        }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }

        screenModelScope.launch(Dispatchers.IO) {
            updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val user = siteRepository.getCurrentUser(auth.orEmpty()) ?: return@launch
            val refreshing = currentState.refreshing
            val section = currentState.section
            val sortType = currentState.sortType
            if (section == SavedItemsSection.Posts) {
                val itemList = userRepository.getSavedPosts(
                    auth = auth,
                    id = user.id,
                    page = currentPage,
                    sort = sortType,
                )
                if (!itemList.isNullOrEmpty()) {
                    currentPage++
                }
                updateState {
                    val newPosts = if (refreshing) {
                        itemList.orEmpty()
                    } else {
                        it.posts + itemList.orEmpty()
                    }
                    it.copy(
                        posts = newPosts,
                        loading = false,
                        canFetchMore = itemList?.isEmpty() != true,
                        refreshing = false,
                    )
                }
            } else {
                val itemList = userRepository.getSavedComments(
                    auth = auth,
                    id = user.id,
                    page = currentPage,
                    sort = sortType,
                )
                if (!itemList.isNullOrEmpty()) {
                    currentPage++
                }
                updateState {
                    val newComments = if (refreshing) {
                        itemList.orEmpty()
                    } else {
                        it.comments + itemList.orEmpty()
                    }
                    it.copy(
                        comments = newComments,
                        loading = false,
                        canFetchMore = itemList?.isEmpty() != true,
                        refreshing = false,
                    )
                }
            }
        }
    }

    private fun applySortType(value: SortType) {
        if (uiState.value.sortType == value) {
            return
        }
        updateState { it.copy(sortType = value) }
        refresh()
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

    private fun handlePostDelete(id: Int) {
        updateState { it.copy(posts = it.posts.filter { post -> post.id != id }) }
    }

    private fun changeSection(section: SavedItemsSection) {
        updateState {
            it.copy(
                section = section,
            )
        }
    }

    private fun toggleUpVotePost(post: PostModel) {
        val newValue = post.myVote <= 0
        val newPost = postRepository.asUpVoted(
            post = post,
            voted = newValue,
        )
        handlePostUpdate(newPost)
        screenModelScope.launch(Dispatchers.IO) {
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

    private fun toggleDownVotePost(post: PostModel) {
        val newValue = post.myVote >= 0
        val newPost = postRepository.asDownVoted(
            post = post,
            downVoted = newValue,
        )
        handlePostUpdate(newPost)
        screenModelScope.launch(Dispatchers.IO) {
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
                handlePostUpdate(post)
            }
        }
    }

    private fun toggleSavePost(post: PostModel) {
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

    private fun toggleUpVoteComment(comment: CommentModel) {
        val newValue = comment.myVote <= 0
        val newComment = commentRepository.asUpVoted(
            comment = comment,
            voted = newValue,
        )
        handleCommentUpdate(newComment)
        screenModelScope.launch(Dispatchers.IO) {
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
        screenModelScope.launch(Dispatchers.IO) {
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
        screenModelScope.launch(Dispatchers.IO) {
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
}
