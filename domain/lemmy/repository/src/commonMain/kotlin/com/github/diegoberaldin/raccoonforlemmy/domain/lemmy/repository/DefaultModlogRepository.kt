package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItemType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultModlogRepository(
    private val services: ServiceProvider,
) : ModlogRepository {
    override suspend fun getItems(
        auth: String?,
        communityId: Long?,
        limit: Int,
        page: Int,
        type: ModlogItemType,
    ): List<ModlogItem>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.modLog.getItems(
                        authHeader = auth.toAuthHeader(),
                        auth = auth,
                        communityId = communityId,
                        limit = limit,
                        page = page,
                        type = type.toDto(),
                    )
                val result =
                    buildList<ModlogItem> {
                        this += response.added?.map { it.toDto() }.orEmpty()
                        this += response.addedToCommunity?.map { it.toDto() }.orEmpty()
                        this += response.adminPurgedComments?.map { it.toDto() }.orEmpty()
                        this += response.adminPurgedCommunities?.map { it.toDto() }.orEmpty()
                        this += response.adminPurgedPersons?.map { it.toDto() }.orEmpty()
                        this += response.adminPurgedPosts?.map { it.toDto() }.orEmpty()
                        this += response.banned?.map { it.toDto() }.orEmpty()
                        this += response.bannedFromCommunity?.map { it.toDto() }.orEmpty()
                        this += response.featuredPosts?.map { it.toDto() }.orEmpty()
                        this += response.hiddenCommunities?.map { it.toDto() }.orEmpty()
                        this += response.lockedPosts?.map { it.toDto() }.orEmpty()
                        this += response.removedComments?.map { it.toDto() }.orEmpty()
                        this += response.removedCommunities?.map { it.toDto() }.orEmpty()
                        this += response.removedPosts?.map { it.toDto() }.orEmpty()
                        this += response.transferredToCommunity?.map { it.toDto() }.orEmpty()
                    }
                result.sortedByDescending { it.date }
            }.getOrNull()
        }
}
