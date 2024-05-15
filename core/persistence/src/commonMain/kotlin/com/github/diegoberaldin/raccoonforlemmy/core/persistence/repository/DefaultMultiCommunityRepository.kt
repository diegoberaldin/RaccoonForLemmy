package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.MultiCommunityEntity
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultMultiCommunityRepository(
    provider: DatabaseProvider,
) : MultiCommunityRepository {

    private val db = provider.getDatabase()

    override suspend fun getAll(accountId: Long): List<MultiCommunityModel> =
        withContext(Dispatchers.IO) {
            db.multicommunitiesQueries.getAll(accountId)
                .executeAsList().map { it.toModel() }
        }

    override suspend fun getById(id: Long): MultiCommunityModel? =
        withContext(Dispatchers.IO) {
            db.multicommunitiesQueries.getById(id).executeAsOneOrNull()?.toModel()
        }

    override suspend fun create(model: MultiCommunityModel, accountId: Long): Long =
        withContext(Dispatchers.IO) {
            db.multicommunitiesQueries.create(
                name = model.name,
                icon = model.icon,
                communityIds = model.communityIds.joinToString(","),
                account_id = accountId,
            )
            val id = db.multicommunitiesQueries.getBy(name = model.name, account_id = accountId)
                .executeAsOneOrNull()?.id
            id ?: 0L
        }

    override suspend fun update(model: MultiCommunityModel) =
        withContext(Dispatchers.IO) {
            db.multicommunitiesQueries.update(
                name = model.name,
                icon = model.icon,
                communityIds = model.communityIds.joinToString(","),
                id = model.id ?: 0,
            )
        }

    override suspend fun delete(model: MultiCommunityModel) =
        withContext(Dispatchers.IO) {
            db.multicommunitiesQueries.delete(model.id ?: 0)
        }
}

private fun MultiCommunityEntity.toModel() = MultiCommunityModel(
    id = id,
    name = name,
    icon = icon,
    communityIds = communityIds
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.toLong() },
)
