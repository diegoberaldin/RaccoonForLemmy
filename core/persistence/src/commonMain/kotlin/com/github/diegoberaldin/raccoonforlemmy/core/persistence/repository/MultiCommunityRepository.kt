package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel

interface MultiCommunityRepository {
    suspend fun getAll(accountId: Long?): List<MultiCommunityModel>

    suspend fun create(model: MultiCommunityModel, accountId: Long): Long

    suspend fun update(model: MultiCommunityModel)

    suspend fun delete(model: MultiCommunityModel)
}
