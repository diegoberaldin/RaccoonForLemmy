package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkPrivateMessageAsReadForm(
    @SerialName("private_message_id")
    val privateMessageId: PrivateMessageId,
    @SerialName("read")
    val read: Boolean,
    @SerialName("auth")
    val auth: String,
)
