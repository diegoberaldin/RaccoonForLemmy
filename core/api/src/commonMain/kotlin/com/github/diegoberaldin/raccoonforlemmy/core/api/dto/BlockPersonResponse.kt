package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockPersonResponse(
    @SerialName("person_view") val personView: PersonView,
    @SerialName("blocked") val blocked: Boolean,
)