package com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.utils

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository

internal class CommunityPaginator(
    private val communityId: Int,
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
        currentIds: List<Int>,
    ): List<PostModel> {
        val (result, nextPage) = postRepository.getAll(
            auth = auth,
            page = currentPage,
            pageCursor = pageCursor,
            limit = PostRepository.DEFAULT_PAGE_SIZE,
            type = ListingType.All,
            sort = sort,
            communityId = communityId,
        )?.let {
            // prevents accidental duplication
            val posts = it.first
            it.copy(
                first = posts.filter { p1 ->
                    p1.id !in currentIds
                },
            )
        } ?: (null to null)
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
