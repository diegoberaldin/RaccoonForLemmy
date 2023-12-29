package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeletePrivateMessageForm(
    @SerialName("private_message_id")
    val privateMessageId: PrivateMessageId,
    @SerialName("deleted")
    val deleted: Boolean,
    @SerialName("auth")
    val auth: String,
)
