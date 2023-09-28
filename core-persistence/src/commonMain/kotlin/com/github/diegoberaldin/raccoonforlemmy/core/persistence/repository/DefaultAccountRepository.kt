package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.AccountEntity
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DefaultAccountRepository(
    private val provider: DatabaseProvider,
) : AccountRepository {

    private val db = provider.getDatabase()

    override suspend fun getAll(): List<AccountModel> = withContext(Dispatchers.IO) {
        db.schemaQueries.getAllAccounts().executeAsList().map { it.toModel() }
    }

    override suspend fun getBy(username: String, instance: String) = withContext(Dispatchers.IO) {
        db.schemaQueries.getAccountBy(username, instance).executeAsOneOrNull()?.toModel()
    }

    override suspend fun createAccount(
        account: AccountModel,
    ) = withContext(Dispatchers.IO) {
        db.schemaQueries.createAccount(
            username = account.username,
            instance = account.instance,
            jwt = account.jwt,
            avatar = account.avatar,
        )
        val entity =
            db.schemaQueries.getAllAccounts().executeAsList().firstOrNull { it.jwt == account.jwt }
        entity?.id ?: 0
    }

    override suspend fun setActive(id: Long, active: Boolean) = withContext(Dispatchers.IO) {
        if (active) {
            db.schemaQueries.markAccountActive(id)
        } else {
            db.schemaQueries.markAccountInactive(id)
        }
    }

    override suspend fun getActive() = withContext(Dispatchers.IO) {
        val entity = db.schemaQueries.getActiveAccount().executeAsOneOrNull()
        entity?.toModel()
    }

    override suspend fun update(id: Long, avatar: String?, jwt: String?) =
        withContext(Dispatchers.IO) {
            db.schemaQueries.updateAccount(
                jwt = jwt,
                avatar = avatar,
                id = id,
            )
        }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        db.schemaQueries.deleteAccount(id)
    }
}

private fun AccountEntity.toModel() = AccountModel(
    id = id,
    username = username,
    instance = instance,
    jwt = jwt.orEmpty(),
    active = active != 0L,
    avatar = avatar,
)