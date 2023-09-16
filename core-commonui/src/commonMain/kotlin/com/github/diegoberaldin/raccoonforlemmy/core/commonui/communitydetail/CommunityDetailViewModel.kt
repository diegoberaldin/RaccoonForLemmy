package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.KeyStoreKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class CommunityDetailViewModel(
    private val mvi: DefaultMviModel<CommunityDetailMviModel.Intent, CommunityDetailMviModel.UiState, CommunityDetailMviModel.Effect>,
    private val community: CommunityModel,
    private val otherInstance: String,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val postsRepository: PostsRepository,
    private val siteRepository: SiteRepository,
    private val keyStore: TemporaryKeyStore,
    private val hapticFeedback: HapticFeedback,
) : MviModel<CommunityDetailMviModel.Intent, CommunityDetailMviModel.UiState, CommunityDetailMviModel.Effect> by mvi,
    ScreenModel {
    private var currentPage: Int = 1
    override fun onStarted() {
        mvi.onStarted()

        val sortType = keyStore[KeyStoreKeys.DefaultPostSortType, 0].toSortType()
        mvi.updateState {
            it.copy(
                community = community,
                sortType = sortType,
                blurNsfw = keyStore[KeyStoreKeys.BlurNsfw, true],
            )
        }

        mvi.scope.launch(Dispatchers.IO) {
            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                mvi.updateState { it.copy(currentUserId = user?.id ?: 0) }
            }
            if (mvi.uiState.value.posts.isEmpty()) {
                refresh()
            }
        }
    }

    override fun reduce(intent: CommunityDetailMviModel.Intent) {
        when (intent) {
            CommunityDetailMviModel.Intent.LoadNextPage -> loadNextPage()
            CommunityDetailMviModel.Intent.Refresh -> refresh()

            is CommunityDetailMviModel.Intent.DownVotePost -> toggleDownVotePost(
                post = uiState.value.posts[intent.index],
                feedback = intent.feedback,
            )

            is CommunityDetailMviModel.Intent.SavePost -> toggleSavePost(
                post = uiState.value.posts[intent.index],
                feedback = intent.feedback,
            )

            is CommunityDetailMviModel.Intent.UpVotePost -> toggleUpVotePost(
                post = uiState.value.posts[intent.index],
                feedback = intent.feedback,
            )

            CommunityDetailMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is CommunityDetailMviModel.Intent.ChangeSort -> applySortType(intent.value)
            CommunityDetailMviModel.Intent.Subscribe -> subscribe()
            CommunityDetailMviModel.Intent.Unsubscribe -> unsubscribe()
            is CommunityDetailMviModel.Intent.DeletePost -> handlePostDelete(intent.id)
        }
    }

    private fun refresh() {
        currentPage = 1
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        mvi.scope.launch(Dispatchers.IO) {
            val community = communityRepository.get(
                auth = identityRepository.authToken.value,
                id = community.id,
            )
            if (community != null) {
                mvi.updateState { it.copy(community = community) }
            }
        }
        loadNextPage()
    }

    private fun applySortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        refresh()
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val refreshing = currentState.refreshing
            val sort = currentState.sortType
            val itemList = if (otherInstance.isNotEmpty()) {
                postsRepository.getAllInInstance(
                    instance = otherInstance,
                    communityId = community.id,
                    page = currentPage,
                    sort = sort,
                )
            } else {
                postsRepository.getAll(
                    auth = auth,
                    communityId = community.id,
                    page = currentPage,
                    sort = sort,
                )
            }
            currentPage++
            val canFetchMore = itemList.size >= CommentRepository.DEFAULT_PAGE_SIZE
            mvi.updateState {
                val newItems = if (refreshing) {
                    itemList
                } else {
                    it.posts + itemList
                }
                it.copy(
                    posts = newItems,
                    loading = false,
                    canFetchMore = canFetchMore,
                    refreshing = false,
                )
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
        val newPost = postsRepository.asUpVoted(
            post = post,
            voted = newValue,
        )
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
        mvi.scope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postsRepository.upVote(
                    auth = auth,
                    post = post,
                    voted = newValue,
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

    private fun toggleDownVotePost(
        post: PostModel,
        feedback: Boolean,
    ) {
        val newValue = post.myVote >= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newPost = postsRepository.asDownVoted(
            post = post,
            downVoted = newValue,
        )
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
        mvi.scope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postsRepository.downVote(
                    auth = auth,
                    post = post,
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

    private fun toggleSavePost(
        post: PostModel,
        feedback: Boolean,
    ) {
        val newValue = !post.saved
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newPost = postsRepository.asSaved(
            post = post,
            saved = newValue,
        )
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
        mvi.scope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postsRepository.save(
                    auth = auth,
                    post = post,
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

    private fun subscribe() {
        hapticFeedback.vibrate()
        mvi.scope.launch(Dispatchers.IO) {
            val community = communityRepository.subscribe(
                auth = identityRepository.authToken.value,
                id = community.id,
            )
            if (community != null) {
                mvi.updateState { it.copy(community = community) }
            }
        }
    }

    private fun unsubscribe() {
        hapticFeedback.vibrate()
        mvi.scope.launch(Dispatchers.IO) {
            val community = communityRepository.unsubscribe(
                auth = identityRepository.authToken.value,
                id = community.id,
            )
            if (community != null) {
                mvi.updateState { it.copy(community = community) }
            }
        }
    }

    private fun handlePostDelete(id: Int) {
        mvi.updateState { it.copy(posts = it.posts.filter { post -> post.id != id }) }
    }
}
