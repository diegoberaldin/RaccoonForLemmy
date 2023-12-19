package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentReportForm(
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("reason") val reason: String?,
    @SerialName("auth") val auth: String,
)
