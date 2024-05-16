package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DraftEntity
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class DefaultDraftRepository(
    provider: DatabaseProvider,
) : DraftRepository {
    private val db = provider.getDatabase()

    override suspend fun getAll(
        type: DraftType,
        accountId: Long,
    ): List<DraftModel> =
        withContext(Dispatchers.IO) {
            db.draftsQueries.getAllBy(type = type.toLong(), account_id = accountId)
                .executeAsList()
                .map { it.toModel() }
        }

    override suspend fun getBy(id: Long): DraftModel? =
        withContext(Dispatchers.IO) {
            db.draftsQueries.getBy(id).executeAsOneOrNull()?.toModel()
        }

    override suspend fun create(
        model: DraftModel,
        accountId: Long,
    ) = withContext(Dispatchers.IO) {
        db.draftsQueries.create(
            type = model.type.toLong(),
            body = model.body,
            title = model.title,
            url = model.url,
            communityId = model.communityId?.toLong(),
            postId = model.postId?.toLong(),
            parentId = model.parentId?.toLong(),
            languageId = model.languageId?.toLong(),
            nsfw = model.nsfw?.let { if (it) 0L else 1L },
            date = model.date,
            info = model.reference,
            account_id = accountId,
        )
    }

    override suspend fun update(model: DraftModel) =
        withContext(Dispatchers.IO) {
            db.draftsQueries.update(
                id = model.id ?: 0,
                body = model.body,
                title = model.title,
                url = model.url,
                communityId = model.communityId?.toLong(),
                languageId = model.languageId?.toLong(),
                date = model.date,
                info = model.reference,
                nsfw = model.nsfw?.let { if (it) 0L else 1L },
            )
        }

    override suspend fun delete(id: Long) =
        withContext(Dispatchers.IO) {
            db.draftsQueries.delete(id)
        }
}

private fun DraftType.toLong(): Long =
    when (this) {
        DraftType.Comment -> 1L
        DraftType.Post -> 0L
    }

private fun Long.toDraftType(): DraftType =
    when (this) {
        1L -> DraftType.Comment
        else -> DraftType.Post
    }

private fun DraftEntity.toModel() =
    DraftModel(
        id = id,
        type = type.toDraftType(),
        title = title,
        body = body,
        url = url,
        communityId = communityId,
        languageId = languageId,
        postId = postId,
        parentId = parentId,
        nsfw = nsfw?.let { it != 0L },
        date = date,
        reference = info,
    )
