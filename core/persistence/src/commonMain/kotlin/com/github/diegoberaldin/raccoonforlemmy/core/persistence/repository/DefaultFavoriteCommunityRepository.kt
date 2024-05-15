package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.FavoriteCommunityEntity
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.FavoriteCommunityModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultFavoriteCommunityRepository(
    provider: DatabaseProvider,
) : FavoriteCommunityRepository {

    private val db = provider.getDatabase()

    override suspend fun getAll(accountId: Long?): List<FavoriteCommunityModel> =
        withContext(Dispatchers.IO) {
            db.favoritecommunitiesQueries.getAll(accountId).executeAsList().map { it.toModel() }
        }

    override suspend fun getBy(
        accountId: Long?,
        communityId: Long,
    ): FavoriteCommunityModel? = withContext(Dispatchers.IO) {
        db.favoritecommunitiesQueries.getBy(
            communityId = communityId.toLong(),
            account_id = accountId,
        ).executeAsOneOrNull()?.toModel()
    }

    override suspend fun create(model: FavoriteCommunityModel, accountId: Long): Long =
        withContext(Dispatchers.IO) {
            val communityId = model.communityId?.toLong() ?: return@withContext 0L
            db.favoritecommunitiesQueries.create(
                communityId = communityId,
                account_id = accountId,
            )
            val id = db.favoritecommunitiesQueries.getBy(
                communityId = communityId,
                account_id = accountId,
            ).executeAsOneOrNull()?.id
            id ?: 0L
        }

    override suspend fun delete(accountId: Long?, model: FavoriteCommunityModel) =
        withContext(Dispatchers.IO) {
            val communityId = model.communityId?.toLong() ?: return@withContext
            val id = db.favoritecommunitiesQueries.getBy(
                communityId = communityId,
                account_id = accountId,
            ).executeAsOneOrNull()?.id ?: return@withContext
            db.favoritecommunitiesQueries.delete(id)
        }
}

private fun FavoriteCommunityEntity.toModel() = FavoriteCommunityModel(
    id = id,
    communityId = communityId,
)
