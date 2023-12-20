package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItemType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto

internal class DefaultModlogRepository(
    private val services: ServiceProvider,
) : ModlogRepository {
    override suspend fun getItems(
        auth: String?,
        communityId: Int,
        limit: Int,
        page: Int,
        type: ModlogItemType,
    ): List<ModlogItem>? = runCatching {
        val response = services.modLog.getItems(
            authHeader = auth.toAuthHeader(),
            auth = auth,
            communityId = communityId,
            limit = limit,
            page = page,
            type = type.toDto(),
        )
        val dto = response.body() ?: return@runCatching null
        val result = buildList<ModlogItem> {
            this += dto.addedToCommunity?.map { it.toDto() }.orEmpty()
            this += dto.bannedFromCommunity?.map { it.toDto() }.orEmpty()
            this += dto.featuredPosts?.map { it.toDto() }.orEmpty()
            this += dto.lockedPosts?.map { it.toDto() }.orEmpty()
            this += dto.removedPosts?.map { it.toDto() }.orEmpty()
            this += dto.removedComments?.map { it.toDto() }.orEmpty()
            this += dto.transferredToCommunity?.map { it.toDto() }.orEmpty()
        }
        result.sortedByDescending { it.date }
    }.getOrNull()
}
