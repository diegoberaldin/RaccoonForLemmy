package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoveCommentForm(
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("reason") val reason: String?,
    @SerialName("removed") val removed: Boolean,
    @SerialName("auth") val auth: String,
)
