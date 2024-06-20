package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

interface CommunityPaginationManager {
    val canFetchMore: Boolean
    val history: List<CommunityModel>

    fun reset(specification: CommunityPaginationSpecification)

    suspend fun loadNextPage(): List<CommunityModel>

    suspend fun fetchAll(): List<CommunityModel>
}
