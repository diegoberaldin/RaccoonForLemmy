package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommunityView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.FollowCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SubscribedType
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
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
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        listingType: ListingType = ListingType.All,
        sortType: SortType = SortType.Active,
        resultType: SearchResultType = SearchResultType.All,
    ): List<Any> = runCatching {
        val response = services.search.search(
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
        posts + comments + communities + users
    }.getOrElse { emptyList() }

    suspend fun getAllInInstance(
        instance: String = "",
        auth: String? = null,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        sort: SortType = SortType.Active,
    ): List<CommunityModel> = runCatching {
        customServices.changeInstance(instance)
        val response = customServices.community.getAll(
            auth = auth,
            page = page,
            limit = limit,
            sort = sort.toDto(),
        ).body()
        response?.communities?.map {
            it.toModel()
        }.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun getSubscribed(
        auth: String? = null,
    ): List<CommunityModel> = runCatching {
        val response = services.site.get(auth).body()
        response?.myUser?.follows?.map { it.community.toModel() }.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun get(
        auth: String? = null,
        id: Int,
    ): CommunityModel? = runCatching {
        val response = services.community.get(
            auth = auth,
            id = id,
        ).body()
        response?.communityView?.toModel()
    }.getOrNull()

    suspend fun subscribe(
        auth: String? = null,
        id: Int,
    ): CommunityModel? = runCatching {
        val data = FollowCommunityForm(
            auth = auth.orEmpty(),
            communityId = id,
            follow = true,
        )
        val response = services.community.follow(data)
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
        val response = services.community.follow(data)
        response.body()?.communityView?.toModel()
    }.getOrNull()
}

private fun CommunityView.toModel() = community.toModel().copy(
    subscribed = when (subscribed) {
        SubscribedType.Subscribed -> true
        SubscribedType.NotSubscribed -> false
        else -> null
    },
)
