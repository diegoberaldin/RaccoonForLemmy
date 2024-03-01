package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItemType

interface ModlogRepository {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }

    suspend fun getItems(
        auth: String? = null,
        communityId: Int?,
        limit: Int = DEFAULT_PAGE_SIZE,
        page: Int,
        type: ModlogItemType = ModlogItemType.All,
    ): List<ModlogItem>?
}