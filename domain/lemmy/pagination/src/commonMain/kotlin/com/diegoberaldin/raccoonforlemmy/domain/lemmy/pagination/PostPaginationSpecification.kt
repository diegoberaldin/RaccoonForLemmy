package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

sealed interface PostPaginationSpecification {
    data class Listing(
        val listingType: ListingType = ListingType.All,
        val sortType: SortType = SortType.Active,
        val includeNsfw: Boolean = true,
    ) : PostPaginationSpecification

    data class MultiCommunity(
        val communityIds: List<Long>,
        val sortType: SortType = SortType.Active,
        val includeNsfw: Boolean = true,
    ) : PostPaginationSpecification

    data class Community(
        val id: Long? = null,
        val name: String? = null,
        val otherInstance: String? = null,
        val query: String? = null,
        val sortType: SortType = SortType.Active,
        val includeNsfw: Boolean = true,
    ) : PostPaginationSpecification

    data class User(
        val id: Long? = null,
        val name: String? = null,
        val otherInstance: String? = null,
        val sortType: SortType = SortType.New,
        val includeNsfw: Boolean = true,
    ) : PostPaginationSpecification

    data class Votes(
        val liked: Boolean = true,
        val sortType: SortType = SortType.New,
    ) : PostPaginationSpecification

    data class Saved(
        val sortType: SortType = SortType.Active,
    ) : PostPaginationSpecification
}
