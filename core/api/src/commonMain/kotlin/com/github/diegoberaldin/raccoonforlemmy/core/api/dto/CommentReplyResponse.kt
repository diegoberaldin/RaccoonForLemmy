package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentReplyResponse(
    @SerialName("comment_reply_view")
    val commentReplyView: CommentReplyView,
)
