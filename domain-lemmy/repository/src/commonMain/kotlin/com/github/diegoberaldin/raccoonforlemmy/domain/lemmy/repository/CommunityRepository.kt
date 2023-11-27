package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BanFromCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.FollowCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

class CommunityRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) {

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
    ): List<SearchResult>? = runCatching {
        if (instance.isNullOrEmpty()) {
            val response = services.search.search(
                authHeader = auth.toAuthHeader(),
                q = query,
                auth = auth,
                page = page,
                limit = limit,
                type = resultType.toDto(),
                listingType = listingType.toDto(),
                sort = sortType.toDto(),
            ).body()
            val posts = response?.posts?.map { it.toModel() }.orEmpty()
            val comments = response?.comments?.map { it.toModel() }.orEmpty()
            val communities = response?.communities?.map { it.toModel() }.orEmpty()
            val users = response?.users?.map { it.toModel() }.orEmpty()
            // returns everything
            posts.map {
                SearchResult.Post(it)
            } + comments.map {
                SearchResult.Comment(it)
            } + communities.map {
                SearchResult.Community(it)
            } + users.map {
                SearchResult.User(it)
            }
        } else {
            customServices.changeInstance(instance)
            val response = customServices.community.getAll(
                page = page,
                limit = limit,
                sort = sortType.toDto(),
            ).body()
            response?.communities?.map {
                SearchResult.Community(model = it.toModel())
            }.orEmpty()
        }
    }.getOrNull()

    suspend fun getSubscribed(
        auth: String? = null,
    ): List<CommunityModel> = runCatching {
        val response = services.site.get(
            authHeader = auth.toAuthHeader(),
            auth = auth,
        ).body()
        response?.myUser?.follows?.map { it.community.toModel() }.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun get(
        auth: String? = null,
        id: Int? = null,
        name: String? = null,
        instance: String? = null,
    ): CommunityModel? = runCatching {
        val response = if (instance.isNullOrEmpty()) {
            services.community.get(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                id = id,
                name = name,
            ).body()
        } else {
            customServices.changeInstance(instance)
            customServices.community.get(name = name).body()
        }
        response?.communityView?.toModel()
    }.getOrNull()

    suspend fun getModerators(
        auth: String? = null,
        id: Int? = null,
    ): List<UserModel> = runCatching {
        val response = services.community.get(
            authHeader = auth.toAuthHeader(),
            auth = auth,
            id = id,
        ).body()
        response?.moderators?.map {
            it.moderator.toModel()
        }.orEmpty()
    }.getOrElse { emptyList() }


    suspend fun subscribe(
        auth: String? = null,
        id: Int,
    ): CommunityModel? = runCatching {
        val data = FollowCommunityForm(
            auth = auth.orEmpty(),
            communityId = id,
            follow = true,
        )
        val response = services.community.follow(
            authHeader = auth.toAuthHeader(),
            form = data
        )
        response.body()?.communityView?.toModel()
    }.getOrNull()

    suspend fun unsubscribe(
        auth: String? = null,
        id: Int,
    ): CommunityModel? = runCatching {
        val data = FollowCommunityForm(
            auth = auth.orEmpty(),
            communityId = id,
            follow = false,
        )
        val response = services.community.follow(
            authHeader = auth.toAuthHeader(),
            form = data
        )
        response.body()?.communityView?.toModel()
    }.getOrNull()

    suspend fun block(
        id: Int,
        blocked: Boolean,
        auth: String?,
    ): Result<Unit> = runCatching {
        val data = BlockCommunityForm(
            communityId = id,
            block = blocked,
            auth = auth.orEmpty(),
        )
        services.community.block(
            authHeader = auth.toAuthHeader(),
            form = data,
        )
    }

    suspend fun banUser(
        auth: String?,
        userId: Int,
        communityId: Int,
        ban: Boolean,
        removeData: Boolean = false,
        reason: String? = null,
        expires: Long? = null,
    ): UserModel? = runCatching {
        val data = BanFromCommunityForm(
            auth = auth.orEmpty(),
            ban = ban,
            removeData = removeData,
            personId = userId,
            communityId = communityId,
            reason = reason,
            expires = expires,
        )
        val response = services.community.ban(
            authHeader = auth.toAuthHeader(),
            form = data,
        )
        response.body()?.personView?.toModel()?.copy(banned = ban)
    }.getOrNull()
}
