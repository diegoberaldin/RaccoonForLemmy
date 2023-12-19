package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateMessagesResponse(
    @SerialName("private_messages")
    val privateMessages: List<PrivateMessageView>,
)
