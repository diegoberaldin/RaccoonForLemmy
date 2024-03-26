package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentLikeForm(
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("score") val score: Int,
    @SerialName("auth") val auth: String,
)
