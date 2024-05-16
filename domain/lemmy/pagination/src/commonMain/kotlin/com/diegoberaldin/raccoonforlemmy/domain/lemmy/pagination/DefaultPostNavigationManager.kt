package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultPostNavigationManager(
    private val postPaginationManager: PostPaginationManager,
) : PostNavigationManager {
    override val canNavigate = MutableStateFlow(false)

    private var states: MutableList<PostPaginationManagerState> = mutableListOf()

    override fun push(state: PostPaginationManagerState) {
        states += state
        canNavigate.value = true
        postPaginationManager.restoreState(state)
    }

    override fun pop() {
        states.removeLast()
        val canStillNavigate = states.isNotEmpty()
        canNavigate.value = canStillNavigate
        if (canStillNavigate) {
            val lastState = states.last()
            postPaginationManager.restoreState(lastState)
        } else {
            postPaginationManager.reset()
        }
    }

    override suspend fun getPrevious(postId: Long): PostModel? {
        val history = postPaginationManager.history
        val index =
            history
                .indexOfFirst { it.id == postId }
                .takeIf { it >= 0 } ?: return null
        return when (index) {
            0 -> null
            else -> history.getOrNull(index - 1)
        }
    }

    override suspend fun getNext(postId: Long): PostModel? {
        val history = postPaginationManager.history
        val index =
            history
                .indexOfFirst { it.id == postId }
                .takeIf { it >= 0 } ?: return null
        return when {
            index < history.lastIndex -> history[index + 1]
            !postPaginationManager.canFetchMore -> null
            else ->
                run {
                    val newPosts = postPaginationManager.loadNextPage()
                    val newIndex = newPosts.indexOfFirst { it.id == postId }
                    newPosts.getOrNull(newIndex + 1)
                }
        }
    }
}
