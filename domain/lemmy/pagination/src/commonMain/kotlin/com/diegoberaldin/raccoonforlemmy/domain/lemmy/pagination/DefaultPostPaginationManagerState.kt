package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel

internal data class DefaultPostPaginationManagerState(
    val specification: PostPaginationSpecification? = null,
    val currentPage: Int = 1,
    val pageCursor: String? = null,
    val history: List<PostModel> = emptyList(),
) : PostPaginationManagerState