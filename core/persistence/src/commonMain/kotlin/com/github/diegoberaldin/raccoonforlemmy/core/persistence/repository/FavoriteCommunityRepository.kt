package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.FavoriteCommunityModel

interface FavoriteCommunityRepository {
    suspend fun getAll(accountId: Long?): List<FavoriteCommunityModel>

    suspend fun getBy(accountId: Long?, communityId: Long): FavoriteCommunityModel?

    suspend fun create(model: FavoriteCommunityModel, accountId: Long): Long

    suspend fun delete(accountId: Long?, model: FavoriteCommunityModel)
}
