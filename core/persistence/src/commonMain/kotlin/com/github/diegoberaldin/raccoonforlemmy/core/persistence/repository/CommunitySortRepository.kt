package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

interface CommunitySortRepository {
    suspend fun get(handle: String): Int?

    suspend fun save(
        handle: String,
        value: Int,
    )

    suspend fun clear()
}
