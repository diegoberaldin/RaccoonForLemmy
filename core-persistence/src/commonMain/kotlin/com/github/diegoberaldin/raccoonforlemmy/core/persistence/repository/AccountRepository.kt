package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel

interface AccountRepository {

    suspend fun getAll(): List<AccountModel>

    suspend fun getBy(username: String, instance: String): AccountModel?

    suspend fun createAccount(account: AccountModel): Long

    suspend fun setActive(id: Long, active: Boolean)

    suspend fun getActive(): AccountModel?

    suspend fun update(id: Long, avatar: String?, jwt: String?)

    suspend fun delete(id: Long)
}
