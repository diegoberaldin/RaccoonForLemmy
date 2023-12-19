package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

interface CommunityRepository {

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }

    suspend fun getAll(
        query: String = "",
        auth: String? = null,
        instance: String? = null,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        listingType: ListingType = ListingType.All,
        sortType: SortType = SortType.Active,
        resultType: SearchResultType = SearchResultType.All,
    ): List<SearchResult>?

    suspend fun getSubscribed(
        auth: String? = null,
    ): List<CommunityModel>

    suspend fun get(
        auth: String? = null,
        id: Int? = null,
        name: String? = null,
        instance: String? = null,
    ): CommunityModel?

    suspend fun getModerators(
        auth: String? = null,
        id: Int? = null,
    ): List<UserModel>

    suspend fun subscribe(
        auth: String? = null,
        id: Int,
    ): CommunityModel?

    suspend fun unsubscribe(
        auth: String? = null,
        id: Int,
    ): CommunityModel?

    suspend fun block(
        id: Int,
        blocked: Boolean,
        auth: String?,
    ): Result<Unit>

    suspend fun banUser(
        auth: String?,
        userId: Int,
        communityId: Int,
        ban: Boolean,
        removeData: Boolean = false,
        reason: String? = null,
        expires: Long? = null,
    ): UserModel?

    suspend fun addModerator(
        auth: String? = null,
        communityId: Int,
        userId: Int,
        added: Boolean,
    ): List<UserModel>
}
