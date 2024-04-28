package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel

interface CommentPaginationManager {
    val canFetchMore: Boolean

    suspend fun reset(specification: CommentPaginationSpecification)
    suspend fun loadNextPage(): List<CommentModel>
}
