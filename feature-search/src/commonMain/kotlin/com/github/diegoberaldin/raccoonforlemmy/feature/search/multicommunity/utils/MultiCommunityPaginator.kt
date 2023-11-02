package com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.utils

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface MultiCommunityPaginator {
    val canFetchMore: Boolean

    fun setCommunities(ids: List<Int>)
    fun reset()

    suspend fun loadNextPage(
        auth: String? = null,
        sort: SortType,
        currentIds: List<Int> = emptyList(),
    ): List<PostModel>
}