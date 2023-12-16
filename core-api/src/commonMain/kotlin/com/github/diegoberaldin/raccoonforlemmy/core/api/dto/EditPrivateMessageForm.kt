package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditPrivateMessageForm(
    @SerialName("content")
    val content: String,
    @SerialName("private_message_id")
    val privateMessageId: PrivateMessageId,
    @SerialName("auth")
    val auth: String,
)
