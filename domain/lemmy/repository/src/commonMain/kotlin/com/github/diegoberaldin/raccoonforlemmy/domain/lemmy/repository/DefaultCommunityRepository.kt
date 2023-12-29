package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.AddModToCommunityForm
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

internal class DefaultCommunityRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) : CommunityRepository {

    override suspend fun getAll(
        query: String,
        auth: String?,
        instance: String?,
        page: Int,
        limit: Int,
        listingType: ListingType,
        sortType: SortType,
        resultType: SearchResultType,
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

    override suspend fun getSubscribed(
        auth: String?,
    ): List<CommunityModel> = runCatching {
        val response = services.site.get(
            authHeader = auth.toAuthHeader(),
            auth = auth,
        ).body()
        response?.myUser?.follows?.map { it.community.toModel() }.orEmpty()
    }.getOrElse { emptyList() }

    override suspend fun get(
        auth: String?,
        id: Int?,
        name: String?,
        instance: String?,
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

    override suspend fun getModerators(
        auth: String?,
        id: Int?,
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

    override suspend fun subscribe(
        auth: String?,
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

    override suspend fun unsubscribe(
        auth: String?,
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

    override suspend fun block(
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
        Unit
    }

    override suspend fun banUser(
        auth: String?,
        userId: Int,
        communityId: Int,
        ban: Boolean,
        removeData: Boolean,
        reason: String?,
        expires: Long?,
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

    override suspend fun addModerator(
        auth: String?,
        communityId: Int,
        userId: Int,
        added: Boolean,
    ): List<UserModel> = runCatching {
        val data = AddModToCommunityForm(
            auth = auth.orEmpty(),
            added = added,
            personId = userId,
            communityId = communityId,
        )
        val response = services.community.addMod(
            authHeader = auth.toAuthHeader(),
            form = data,
        ).body()
        response?.moderators?.map {
            it.moderator.toModel()
        }.orEmpty()
    }.getOrElse { emptyList() }
}
