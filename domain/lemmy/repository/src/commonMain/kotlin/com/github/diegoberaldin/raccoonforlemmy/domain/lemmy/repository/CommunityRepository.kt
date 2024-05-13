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

    suspend fun search(
        query: String = "",
        auth: String? = null,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        communityId: Long? = null,
        instance: String? = null,
        listingType: ListingType = ListingType.All,
        sortType: SortType = SortType.Active,
        resultType: SearchResultType = SearchResultType.All,
    ): List<SearchResult>

    suspend fun getList(
        instance: String,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        sortType: SortType = SortType.Active,
    ): List<CommunityModel>

    suspend fun getResolved(
        query: String,
        auth: String? = null,
    ): CommunityModel?

    suspend fun getSubscribed(
        auth: String? = null,
    ): List<CommunityModel>

    suspend fun get(
        auth: String? = null,
        id: Long? = null,
        name: String? = null,
        instance: String? = null,
    ): CommunityModel?

    suspend fun getModerators(
        auth: String? = null,
        id: Long? = null,
    ): List<UserModel>

    suspend fun subscribe(
        auth: String? = null,
        id: Long,
    ): CommunityModel?

    suspend fun unsubscribe(
        auth: String? = null,
        id: Long,
    ): CommunityModel?

    suspend fun block(
        id: Long,
        blocked: Boolean,
        auth: String?,
    ): Result<Unit>

    suspend fun banUser(
        auth: String?,
        userId: Long,
        communityId: Long,
        ban: Boolean,
        removeData: Boolean = false,
        reason: String? = null,
        expires: Long? = null,
    ): UserModel?

    suspend fun addModerator(
        auth: String? = null,
        communityId: Long,
        userId: Long,
        added: Boolean,
    ): List<UserModel>

    suspend fun update(
        auth: String? = null,
        community: CommunityModel,
    )

    suspend fun hide(
        auth: String?,
        communityId: Long,
        hidden: Boolean,
        reason: String? = null,
    )

    suspend fun purge(
        auth: String?,
        communityId: Long,
        reason: String? = null,
    )
}
