package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePrivateMessageForm(
    @SerialName("content")
    val content: String,
    @SerialName("recipient_id")
    val recipientId: PersonId,
    @SerialName("auth")
    val auth: String,
)
