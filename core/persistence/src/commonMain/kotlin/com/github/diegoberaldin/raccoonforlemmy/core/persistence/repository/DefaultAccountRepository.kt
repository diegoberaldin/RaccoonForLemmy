package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.AccountEntity
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultAccountRepository(
    provider: DatabaseProvider,
) : AccountRepository {
    private val db = provider.getDatabase()

    override suspend fun getAll(): List<AccountModel> =
        withContext(Dispatchers.IO) {
            db.accountsQueries.getAll().executeAsList().map { it.toModel() }
        }

    override suspend fun getBy(
        username: String,
        instance: String,
    ) = withContext(Dispatchers.IO) {
        db.accountsQueries.getBy(username, instance).executeAsOneOrNull()?.toModel()
    }

    override suspend fun createAccount(account: AccountModel) =
        withContext(Dispatchers.IO) {
            db.accountsQueries.create(
                username = account.username,
                instance = account.instance,
                jwt = account.jwt,
                avatar = account.avatar,
            )
            val entity =
                db.accountsQueries.getAll().executeAsList().firstOrNull { it.jwt == account.jwt }
            entity?.id ?: 0
        }

    override suspend fun setActive(
        id: Long,
        active: Boolean,
    ) = withContext(Dispatchers.IO) {
        if (active) {
            db.accountsQueries.setActive(id)
        } else {
            db.accountsQueries.setInactive(id)
        }
    }

    override suspend fun getActive() =
        withContext(Dispatchers.IO) {
            val entity = db.accountsQueries.getActive().executeAsOneOrNull()
            entity?.toModel()
        }

    override suspend fun update(
        id: Long,
        avatar: String?,
        jwt: String?,
    ) = withContext(Dispatchers.IO) {
        db.accountsQueries.update(
            jwt = jwt,
            avatar = avatar,
            id = id,
        )
    }

    override suspend fun delete(id: Long) =
        withContext(Dispatchers.IO) {
            db.accountsQueries.delete(id)
        }
}

private fun AccountEntity.toModel() =
    AccountModel(
        id = id,
        username = username,
        instance = instance,
        jwt = jwt.orEmpty(),
        active = active != 0L,
        avatar = avatar,
    )
