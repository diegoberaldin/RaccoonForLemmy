package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository

internal class DefaultMultiCommunityPaginator(
    private val postRepository: PostRepository,
) : MultiCommunityPaginator {
    private var paginators = emptyList<Paginator>()

    override val canFetchMore: Boolean
        get() = paginators.any { it.canFetchMore }

    override fun setCommunities(ids: List<Long>) {
        paginators = ids.map {
            Paginator(
                communityId = it,
                postRepository = postRepository,
            )
        }
    }

    override fun reset() {
        paginators.forEach { it.reset() }
    }

    override suspend fun loadNextPage(
        auth: String?,
        sort: SortType,
    ): List<PostModel> = buildList {
        for (paginator in paginators) {
            if (paginator.canFetchMore) {
                val elements = paginator.loadNextPage(
                    auth = auth,
                    sort = sort,
                )
                addAll(elements)
            }
        }
    }.sortedByDescending { it.publishDate }
}

private class Paginator(
    private val communityId: Long,
    private val postRepository: PostRepository,
) {
    private var currentPage: Int = 1
    private var pageCursor: String? = null
    var canFetchMore: Boolean = true
        private set

    fun reset() {
        currentPage = 1
        pageCursor = null
        canFetchMore = true
    }

    suspend fun loadNextPage(
        auth: String?,
        sort: SortType,
    ): List<PostModel> {
        val (result, nextPage) = postRepository.getAll(
            auth = auth,
            page = currentPage,
            pageCursor = pageCursor,
            limit = PostRepository.DEFAULT_PAGE_SIZE,
            type = ListingType.All,
            sort = sort,
            communityId = communityId,
        ) ?: (null to null)
        if (!result.isNullOrEmpty()) {
            currentPage++
        }
        if (nextPage != null) {
            pageCursor = nextPage
        }
        canFetchMore = result?.isEmpty() != true
        return result.orEmpty()
    }
}
