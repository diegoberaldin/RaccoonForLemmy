package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkPersonMentionAsReadForm(
    @SerialName("person_mention_id")
    val mentionId: PersonMentionId,
    @SerialName("read")
    val read: Boolean,
    @SerialName("auth")
    val auth: String,
)
