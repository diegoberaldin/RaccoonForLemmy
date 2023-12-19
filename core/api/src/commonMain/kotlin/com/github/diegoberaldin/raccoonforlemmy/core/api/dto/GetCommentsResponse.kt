package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetCommentsResponse(
    @SerialName("comments")
    val comments: List<CommentView>,
)
