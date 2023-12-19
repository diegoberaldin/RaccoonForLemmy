package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DistinguishCommentForm(
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("distinguished") val distinguished: Boolean,
    @SerialName("auth") val auth: String,
)
