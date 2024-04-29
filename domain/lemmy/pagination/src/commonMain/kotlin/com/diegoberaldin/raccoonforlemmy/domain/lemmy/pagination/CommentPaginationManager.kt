package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel

interface CommentPaginationManager {
    val canFetchMore: Boolean

    fun reset(specification: CommentPaginationSpecification)
    suspend fun loadNextPage(): List<CommentModel>
}
