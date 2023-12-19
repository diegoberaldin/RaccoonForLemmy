package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetRepliesResponse(
    @SerialName("replies")
    val replies: List<CommentReplyView>,
)
