package com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ShareHelper
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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
        notificationCenter.addObserver({
            (it as? PostModel)?.also { post ->
                handlePostUpdate(post)
            }
        }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.PostUpdated)
        notificationCenter.addObserver({
            (it as? PostModel)?.also { post ->
                handlePostDelete(post.id)
            }
        }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.PostDeleted)
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

            identityRepository.authToken.drop(1).onEach {
                mvi.updateState {
                    it.copy(
                        posts = emptyList(),
                        comments = emptyList(),
                    )
                }
                refresh()
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
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: ProfileLoggedMviModel.Intent) {
        when (intent) {
            is ProfileLoggedMviModel.Intent.ChangeSection -> changeSection(intent.section)
            is ProfileLoggedMviModel.Intent.DeleteComment -> deleteComment(intent.id)
            is ProfileLoggedMviModel.Intent.DeletePost -> deletePost(intent.id)
            ProfileLoggedMviModel.Intent.LoadNextPage -> loadNextPage()
            ProfileLoggedMviModel.Intent.Refresh -> refresh()
            is ProfileLoggedMviModel.Intent.SharePost -> share(
                post = uiState.value.posts[intent.index]
            )

            is ProfileLoggedMviModel.Intent.DownVoteComment -> toggleDownVoteComment(
                comment = uiState.value.comments[intent.index],
                feedback = intent.feedback,
            )

            is ProfileLoggedMviModel.Intent.DownVotePost -> toggleDownVotePost(
                post = uiState.value.posts[intent.index],
                feedback = intent.feedback,
            )

            is ProfileLoggedMviModel.Intent.SaveComment -> toggleSaveComment(
                comment = uiState.value.comments[intent.index],
                feedback = intent.feedback,
            )

            is ProfileLoggedMviModel.Intent.SavePost -> toggleSavePost(
                post = uiState.value.posts[intent.index],
                feedback = intent.feedback,
            )

            is ProfileLoggedMviModel.Intent.UpVoteComment -> toggleUpVoteComment(
                comment = uiState.value.comments[intent.index],
                feedback = intent.feedback,
            )

            is ProfileLoggedMviModel.Intent.UpVotePost -> toggleUpVotePost(
                post = uiState.value.posts[intent.index],
                feedback = intent.feedback,
            )
        }
    }

    private fun refresh(initial: Boolean = false) {
        currentPage = 1
        mvi.scope?.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            val user = siteRepository.getCurrentUser(auth)
            mvi.updateState {
                it.copy(
                    user = user,
                    canFetchMore = true,
                    refreshing = true,
                    initial = initial,
                )
            }
            loadNextPage()
        }
    }

    private fun changeSection(section: ProfileLoggedSection) {
        currentPage = 1
        mvi.updateState {
            it.copy(
                section = section,
            )
        }
        refresh(initial = true)
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading || currentState.user == null) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
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
