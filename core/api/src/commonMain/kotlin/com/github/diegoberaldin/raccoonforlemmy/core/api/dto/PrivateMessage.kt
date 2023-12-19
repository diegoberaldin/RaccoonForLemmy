package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateMessage(
    @SerialName("id")
    val id: PrivateMessageId,
    @SerialName("creator_id")
    val creatorId: PersonId,
    @SerialName("recipient_id")
    val recipientId: PersonId,
    @SerialName("content")
    val content: String,
    @SerialName("deleted")
    val deleted: Boolean,
    @SerialName("read")
    val read: Boolean,
    @SerialName("published")
    val published: String,
    @SerialName("? ")
    val updated: String? = null,
    @SerialName("ap_id")
    val apId: String,
    @SerialName("local")
    val local: Boolean,
)
