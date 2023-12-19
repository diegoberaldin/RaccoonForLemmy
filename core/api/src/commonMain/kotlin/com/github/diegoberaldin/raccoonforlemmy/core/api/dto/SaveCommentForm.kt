package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaveCommentForm(
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("save") val save: Boolean,
    @SerialName("auth") val auth: String,
)