package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonMention(
    @SerialName("id") val id: PersonMentionId,
    @SerialName("recipient_id") val recipientId: PersonId,
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("read") val read: Boolean,
    @SerialName("published") val published: String,
)