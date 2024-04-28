package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

sealed interface CommentPaginationSpecification {

    val history: List<CommentModel>

    data class Replies(
        val postId: Long? = null,
        val listingType: ListingType? = null,
        val otherInstance: String? = null,
        val sortType: SortType = SortType.Active,
        override val history: List<CommentModel> = emptyList(),
    ) : CommentPaginationSpecification

    data class User(
        val id: Long? = null,
        val name: String? = null,
        val otherInstance: String? = null,
        val sortType: SortType = SortType.New,
        override val history: List<CommentModel> = emptyList(),
    ) : CommentPaginationSpecification

    data class Votes(
        val liked: Boolean = true,
        val sortType: SortType = SortType.New,
        override val history: List<CommentModel> = emptyList(),
    ) : CommentPaginationSpecification
}
