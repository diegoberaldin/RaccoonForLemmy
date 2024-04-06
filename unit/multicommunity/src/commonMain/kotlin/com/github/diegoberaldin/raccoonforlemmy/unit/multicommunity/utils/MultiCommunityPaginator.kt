package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.utils

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface MultiCommunityPaginator {
    val canFetchMore: Boolean

    fun setCommunities(ids: List<Long>)
    fun reset()

    suspend fun loadNextPage(
        auth: String? = null,
        sort: SortType,
        currentIds: List<Long> = emptyList(),
    ): List<PostModel>
}
