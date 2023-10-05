package com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.shareUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.utils.MultiCommunityPaginator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MultiCommunityViewModel(
    private val mvi: DefaultMviModel<MultiCommunityMviModel.Intent, MultiCommunityMviModel.UiState, MultiCommunityMviModel.Effect>,
    private val community: MultiCommunityModel,
    private val postRepository: PostRepository,
    private val identityRepository: IdentityRepository,
    private val themeRepository: ThemeRepository,
    private val shareHelper: ShareHelper,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
    private val paginator: MultiCommunityPaginator,
) : ScreenModel,
    MviModel<MultiCommunityMviModel.Intent, MultiCommunityMviModel.UiState, MultiCommunityMviModel.Effect> by mvi {

    init {
        notificationCenter.addObserver({
            (it as? PostModel)?.also { post ->
                handlePostUpdate(post)
            }
        }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.PostUpdated)
    }

    fun finalize() {
        notificationCenter.removeObserver(this::class.simpleName.orEmpty())
    }

    override fun onStarted() {
        mvi.onStarted()

        mvi.scope?.launch {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)

            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        blurNsfw = settings.blurNsfw,
                        swipeActionsEnabled = settings.enableSwipeActions,
                    )
                }
            }.launchIn(this)
        }

        mvi.scope?.launch(Dispatchers.IO) {
            if (uiState.value.posts.isEmpty()) {
                val settings = settingsRepository.currentSettings.value
                mvi.updateState {
                    it.copy(
                        sortType = settings.defaultPostSortType.toSortType(),
                    )
                }
                paginator.setCommunities(community.communityIds)
                refresh()
            }
        }
    }

    override fun reduce(intent: MultiCommunityMviModel.Intent) {
        when (intent) {
            is MultiCommunityMviModel.Intent.ChangeSort -> applySortType(intent.value)
            is MultiCommunityMviModel.Intent.DownVotePost -> toggleDownVote(
                post = uiState.value.posts[intent.index],
                feedback = intent.feedback,
            )

            MultiCommunityMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            MultiCommunityMviModel.Intent.LoadNextPage -> loadNextPage()
            MultiCommunityMviModel.Intent.Refresh -> refresh()
            is MultiCommunityMviModel.Intent.SavePost -> toggleSave(
                post = uiState.value.posts[intent.index],
                feedback = intent.feedback,
            )

            is MultiCommunityMviModel.Intent.SharePost -> share(post = uiState.value.posts[intent.index])
            is MultiCommunityMviModel.Intent.UpVotePost -> toggleUpVote(
                post = uiState.value.posts[intent.index],
                feedback = intent.feedback,
            )
        }
    }

    private fun refresh() {
        paginator.reset()
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val sort = currentState.sortType ?: SortType.Active
            val refreshing = currentState.refreshing
            val includeNsfw = settingsRepository.currentSettings.value.includeNsfw

            val postList = paginator.loadNextPage(
                auth = auth,
                sort = sort,
            )
            val canFetchMore = paginator.canFetchMore
            mvi.updateState {
                val newPosts = if (refreshing) {
                    postList
                } else {
                    // prevents accidental duplication
                    it.posts + postList.filter { p -> it.posts.none { e -> e.id == p.id } }
                }.filter { post ->
                    if (includeNsfw) {
                        true
                    } else {
                        !post.nsfw
                    }
                }
                it.copy(
                    posts = newPosts,
                    loading = false,
                    canFetchMore = canFetchMore,
                    refreshing = false,
                )
            }
        }
    }

    private fun applySortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        refresh()
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

    private fun share(post: PostModel) {
        val url = post.shareUrl
        if (url.isNotEmpty()) {
            shareHelper.share(url, "text/plain")
        }
    }
}
