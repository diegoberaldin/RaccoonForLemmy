package com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.shareUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

class ProfileLoggedViewModel(
    private val mvi: DefaultMviModel<ProfileLoggedMviModel.Intent, ProfileLoggedMviModel.UiState, ProfileLoggedMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val shareHelper: ShareHelper,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
) : ProfileLoggedMviModel,
    MviModel<ProfileLoggedMviModel.Intent, ProfileLoggedMviModel.UiState, ProfileLoggedMviModel.Effect> by mvi {

    private var currentPage = 1

    init {
        notificationCenter.addObserver(
            {
                (it as? PostModel)?.also { post ->
                    handlePostUpdate(post)
                }
            }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.PostUpdated
        )
        notificationCenter.addObserver(
            {
                (it as? PostModel)?.also { post ->
                    handlePostDelete(post.id)
                }
            }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.PostDeleted
        )
    }

    fun finalize() {
        notificationCenter.removeObserver(this::class.simpleName.orEmpty())
    }

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch(Dispatchers.IO) {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)

            identityRepository.isLogged.drop(1).debounce(250).onEach { logged ->
                if (logged == true) {
                    mvi.updateState {
                        it.copy(
                            posts = emptyList(),
                            comments = emptyList(),
                        )
                    }
                    refreshUser()
                    refresh()
                }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        separateUpAndDownVotes = settings.separateUpAndDownVotes,
                        autoLoadImages = settings.autoLoadImages,
                        fullHeightImages = settings.fullHeightImages,
                    )
                }
            }.launchIn(this)

            if (uiState.value.posts.isEmpty()) {
                refreshUser()
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: ProfileLoggedMviModel.Intent) {
        when (intent) {
            is ProfileLoggedMviModel.Intent.ChangeSection -> changeSection(intent.section)
            is ProfileLoggedMviModel.Intent.DeleteComment -> deleteComment(intent.id)
            is ProfileLoggedMviModel.Intent.DeletePost -> deletePost(intent.id)
            ProfileLoggedMviModel.Intent.LoadNextPage -> mvi.scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }

            ProfileLoggedMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
                refresh()
            }

            is ProfileLoggedMviModel.Intent.SharePost -> {
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    share(post = post)
                }
            }

            is ProfileLoggedMviModel.Intent.DownVoteComment -> {
                uiState.value.comments.firstOrNull { it.id == intent.id }?.also { comment ->
                    toggleDownVoteComment(
                        comment = comment,
                        feedback = intent.feedback,
                    )
                }
            }

            is ProfileLoggedMviModel.Intent.DownVotePost -> {
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleDownVotePost(
                        post = post,
                        feedback = intent.feedback,
                    )
                }
            }

            is ProfileLoggedMviModel.Intent.SaveComment -> {
                uiState.value.comments.firstOrNull { it.id == intent.id }?.also { comment ->
                    toggleSaveComment(
                        comment = comment,
                        feedback = intent.feedback,
                    )
                }
            }

            is ProfileLoggedMviModel.Intent.SavePost -> {
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleSavePost(
                        post = post,
                        feedback = intent.feedback,
                    )
                }
            }

            is ProfileLoggedMviModel.Intent.UpVoteComment -> {
                uiState.value.comments.firstOrNull { it.id == intent.id }?.also { comment ->
                    toggleUpVoteComment(
                        comment = comment,
                        feedback = intent.feedback,
                    )
                }
            }

            is ProfileLoggedMviModel.Intent.UpVotePost -> {
                uiState.value.posts.firstOrNull { it.id == intent.id }?.also { post ->
                    toggleUpVotePost(
                        post = post,
                        feedback = intent.feedback,
                    )
                }
            }
        }
    }

    private suspend fun refreshUser() {
        val auth = identityRepository.authToken.value.orEmpty()
        if (auth.isEmpty()) {
            mvi.updateState { it.copy(user = null) }
        } else {
            var user = siteRepository.getCurrentUser(auth)
            runCatching {
                withTimeout(2000) {
                    while (user == null) {
                        // retry getting user if non-empty auth
                        delay(500)
                        user = siteRepository.getCurrentUser(auth)
                        yield()
                    }
                    mvi.updateState { it.copy(user = user) }
                }
            }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage = 1
        mvi.updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
            )
        }
        loadNextPage()
    }

    private fun changeSection(section: ProfileLoggedSection) {
        currentPage = 1
        mvi.updateState {
            it.copy(
                section = section,
            )
        }

        mvi.scope?.launch(Dispatchers.IO) {
            refresh(initial = true)
        }
    }

    private suspend fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading || currentState.user == null) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val userId = currentState.user.id
        val section = currentState.section
        if (section == ProfileLoggedSection.Posts) {
            val itemList = userRepository.getPosts(
                auth = auth,
                id = userId,
                page = currentPage,
                sort = SortType.New,
            )
            val comments = if (currentPage == 1 && currentState.comments.isEmpty()) {
                // this is needed because otherwise on first selector change
                // the lazy column scrolls back to top (it must have an empty data set)
                userRepository.getComments(
                    auth = auth,
                    id = userId,
                    page = currentPage,
                    sort = SortType.New,
                ).orEmpty()
            } else {
                currentState.comments
            }
            mvi.updateState {
                val newPosts = if (refreshing) {
                    itemList.orEmpty()
                } else {
                    it.posts + itemList.orEmpty()
                }
                it.copy(
                    posts = newPosts,
                    comments = comments,
                    loading = false,
                    canFetchMore = itemList?.isEmpty() != true,
                    refreshing = false,
                )
            }
        } else {
            val itemList = userRepository.getComments(
                auth = auth,
                id = userId,
                page = currentPage,
                sort = SortType.New,
            )
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
        }
        currentPage++
    }

    private fun toggleUpVotePost(post: PostModel, feedback: Boolean) {
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
                posts = it.posts.map { p ->
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

    private fun toggleDownVotePost(post: PostModel, feedback: Boolean) {
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
                posts = it.posts.map { p ->
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

    private fun toggleSavePost(post: PostModel, feedback: Boolean) {
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
                posts = it.posts.map { p ->
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
                comments = it.comments.map { c ->
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
                comments = it.comments.map { c ->
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
                comments = it.comments.map { c ->
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
        }
    }

    private fun handlePostUpdate(post: PostModel) {
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

    private fun handlePostDelete(id: Int) {
        mvi.updateState { it.copy(posts = it.posts.filter { post -> post.id != id }) }
    }

    private fun deletePost(id: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            postRepository.delete(id = id, auth = auth)
            handlePostDelete(id)
        }
    }

    private fun deleteComment(id: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            commentRepository.delete(id, auth)
            refresh()
        }
    }

    private fun share(post: PostModel) {
        val url = post.shareUrl
        if (url.isNotEmpty()) {
            shareHelper.share(url, "text/plain")
        }
    }
}
