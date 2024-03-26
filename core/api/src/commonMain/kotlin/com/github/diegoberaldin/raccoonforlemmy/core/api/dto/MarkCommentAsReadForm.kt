package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkCommentAsReadForm(
    @SerialName("comment_reply_id")
    val replyId: CommentId,
    @SerialName("read")
    val read: Boolean,
    @SerialName("auth")
    val auth: String,
)
