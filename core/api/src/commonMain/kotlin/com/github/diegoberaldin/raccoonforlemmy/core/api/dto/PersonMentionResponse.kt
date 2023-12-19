package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonMentionResponse(
    @SerialName("person_mention_view")
    val personMentionView: PersonMentionView,
)
