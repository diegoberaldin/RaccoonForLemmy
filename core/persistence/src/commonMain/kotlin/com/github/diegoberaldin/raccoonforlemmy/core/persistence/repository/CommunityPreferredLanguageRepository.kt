package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

interface CommunityPreferredLanguageRepository {
    suspend fun get(handle: String): Long?

    suspend fun save(
        handle: String,
        value: Long?,
    )

    suspend fun clear()
}