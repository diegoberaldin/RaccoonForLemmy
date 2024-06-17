package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

sealed interface CommentPaginationSpecification {
    data class Replies(
        val postId: Long? = null,
        val listingType: ListingType? = null,
        val otherInstance: String? = null,
        val sortType: SortType = SortType.Active,
        val includeDeleted: Boolean = false,
    ) : CommentPaginationSpecification

    data class User(
        val id: Long? = null,
        val name: String? = null,
        val otherInstance: String? = null,
        val sortType: SortType = SortType.New,
        val includeDeleted: Boolean = false,
    ) : CommentPaginationSpecification

    data class Votes(
        val liked: Boolean = true,
        val sortType: SortType = SortType.New,
    ) : CommentPaginationSpecification

    data class Saved(
        val sortType: SortType = SortType.Active,
    ) : CommentPaginationSpecification
}
