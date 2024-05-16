package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.AddModToCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BanFromCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.FollowCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.HideCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PurgeCommunityForm
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultCommunityRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) : CommunityRepository {
    override suspend fun search(
        query: String,
        auth: String?,
        page: Int,
        limit: Int,
        communityId: Long?,
        instance: String?,
        listingType: ListingType,
        sortType: SortType,
        resultType: SearchResultType,
    ): List<SearchResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val searchResponse =
                    if (instance.isNullOrEmpty()) {
                        services.search.search(
                            authHeader = auth.toAuthHeader(),
                            q = query,
                            auth = auth,
                            page = page,
                            limit = limit,
                            communityId = communityId,
                            type = resultType.toDto(),
                            listingType = listingType.toDto(),
                            sort = sortType.toDto(),
                        ).body()
                    } else {
                        customServices.changeInstance(instance)
                        customServices.search.search(
                            authHeader = auth.toAuthHeader(),
                            q = query,
                            auth = auth,
                            page = page,
                            limit = limit,
                            communityId = communityId,
                            type = resultType.toDto(),
                            listingType = listingType.toDto(),
                            sort = sortType.toDto(),
                        ).body()
                    }
                buildList<SearchResult> {
                    val posts = searchResponse?.posts?.map { it.toModel() }.orEmpty()
                    this += posts.map { SearchResult.Post(it) }

                    val comments = searchResponse?.comments?.map { it.toModel() }.orEmpty()
                    this += comments.map { SearchResult.Comment(it) }

                    val communities = searchResponse?.communities?.map { it.toModel() }.orEmpty()
                    this += communities.map { SearchResult.Community(it) }

                    val users = searchResponse?.users?.map { it.toModel() }.orEmpty()
                    this += users.map { SearchResult.User(it) }
                }
            }.getOrElse { emptyList() }
        }

    override suspend fun getList(
        instance: String,
        page: Int,
        limit: Int,
        sortType: SortType,
    ): List<CommunityModel> =
        withContext(Dispatchers.IO) {
            runCatching {
                customServices.changeInstance(instance)
                val response =
                    customServices.community.getAll(
                        page = page,
                        limit = limit,
                        sort = sortType.toDto(),
                    ).body()
                response?.communities?.map {
                    it.toModel()
                }.orEmpty()
            }.getOrElse { emptyList() }
        }

    override suspend fun getResolved(
        query: String,
        auth: String?,
    ): CommunityModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val resolveResponse =
                    services.search.resolveObject(
                        authHeader = auth.toAuthHeader(),
                        q = query,
                    ).body()
                resolveResponse?.community?.toModel()
            }.getOrNull()
        }

    override suspend fun getSubscribed(auth: String?): List<CommunityModel> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.site.get(
                        authHeader = auth.toAuthHeader(),
                        auth = auth,
                    ).body()
                response?.myUser?.follows?.map { it.community.toModel() }.orEmpty()
            }.getOrElse { emptyList() }
        }

    override suspend fun get(
        auth: String?,
        id: Long?,
        name: String?,
        instance: String?,
    ): CommunityModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    if (instance.isNullOrEmpty()) {
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
        }

    override suspend fun getModerators(
        auth: String?,
        id: Long?,
    ): List<UserModel> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.community.get(
                        authHeader = auth.toAuthHeader(),
                        auth = auth,
                        id = id,
                    ).body()
                response?.moderators?.map {
                    it.moderator.toModel()
                }.orEmpty()
            }.getOrElse { emptyList() }
        }

    override suspend fun subscribe(
        auth: String?,
        id: Long,
    ): CommunityModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    FollowCommunityForm(
                        auth = auth.orEmpty(),
                        communityId = id,
                        follow = true,
                    )
                val response =
                    services.community.follow(
                        authHeader = auth.toAuthHeader(),
                        form = data,
                    )
                response.body()?.communityView?.toModel()
            }.getOrNull()
        }

    override suspend fun unsubscribe(
        auth: String?,
        id: Long,
    ): CommunityModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    FollowCommunityForm(
                        auth = auth.orEmpty(),
                        communityId = id,
                        follow = false,
                    )
                val response =
                    services.community.follow(
                        authHeader = auth.toAuthHeader(),
                        form = data,
                    )
                response.body()?.communityView?.toModel()
            }.getOrNull()
        }

    override suspend fun block(
        id: Long,
        blocked: Boolean,
        auth: String?,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    BlockCommunityForm(
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
        }

    override suspend fun banUser(
        auth: String?,
        userId: Long,
        communityId: Long,
        ban: Boolean,
        removeData: Boolean,
        reason: String?,
        expires: Long?,
    ): UserModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    BanFromCommunityForm(
                        auth = auth.orEmpty(),
                        ban = ban,
                        removeData = removeData,
                        personId = userId,
                        communityId = communityId,
                        reason = reason,
                        expires = expires,
                    )
                val response =
                    services.community.ban(
                        authHeader = auth.toAuthHeader(),
                        form = data,
                    )
                response.body()?.personView?.toModel()?.copy(banned = ban)
            }.getOrNull()
        }

    override suspend fun addModerator(
        auth: String?,
        communityId: Long,
        userId: Long,
        added: Boolean,
    ): List<UserModel> =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    AddModToCommunityForm(
                        auth = auth.orEmpty(),
                        added = added,
                        personId = userId,
                        communityId = communityId,
                    )
                val response =
                    services.community.addMod(
                        authHeader = auth.toAuthHeader(),
                        form = data,
                    ).body()
                response?.moderators?.map {
                    it.moderator.toModel()
                }.orEmpty()
            }.getOrElse { emptyList() }
        }

    override suspend fun update(
        auth: String?,
        community: CommunityModel,
    ): Unit =
        withContext(Dispatchers.IO) {
            val data =
                EditCommunityForm(
                    communityId = community.id,
                    icon = community.icon,
                    banner = community.banner,
                    title = community.name,
                    description = community.description,
                    nsfw = community.nsfw,
                    postingRestrictedToMods = community.postingRestrictedToMods,
                )
            val response =
                services.community.edit(
                    authHeader = auth.toAuthHeader(),
                    form = data,
                )
            if (!response.isSuccessful) {
                val error = response.errorBody().toString()
                throw Exception(error)
            }
        }

    override suspend fun hide(
        auth: String?,
        communityId: Long,
        hidden: Boolean,
        reason: String?,
    ) = withContext(Dispatchers.IO) {
        val data =
            HideCommunityForm(
                communityId = communityId,
                reason = reason,
                hidden = hidden,
            )
        val response =
            services.community.hide(
                form = data,
                authHeader = auth.toAuthHeader(),
            )
        require(response.body()?.success == true)
    }

    override suspend fun purge(
        auth: String?,
        communityId: Long,
        reason: String?,
    ) = withContext(Dispatchers.IO) {
        val data =
            PurgeCommunityForm(
                communityId = communityId,
                reason = reason,
            )
        val response =
            services.community.purge(
                form = data,
                authHeader = auth.toAuthHeader(),
            )
        require(response.body()?.success == true)
    }
}
