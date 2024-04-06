package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.utils

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository

class DefaultMultiCommunityPaginator(
    private val postRepository: PostRepository,
) : MultiCommunityPaginator {
    private var paginators = emptyList<CommunityPaginator>()

    override val canFetchMore: Boolean
        get() = paginators.any { it.canFetchMore }

    override fun setCommunities(ids: List<Long>) {
        paginators = ids.map {
            CommunityPaginator(
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
        currentIds: List<Long>,
    ): List<PostModel> = buildList {
        for (paginator in paginators) {
            if (paginator.canFetchMore) {
                val elements = paginator.loadNextPage(
                    auth = auth,
                    sort = sort,
                    currentIds = currentIds,
                )
                addAll(elements)
            }
        }
    }.sortedByDescending { it.publishDate }
}
