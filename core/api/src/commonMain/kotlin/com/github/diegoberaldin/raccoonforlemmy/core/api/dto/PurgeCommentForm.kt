package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurgeCommentForm(
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("reason") val reason: String?,
)
