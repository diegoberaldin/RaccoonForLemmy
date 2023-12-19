package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockPersonForm(
    @SerialName("person_id") val personId: PersonId,
    @SerialName("block") val block: Boolean,
    @SerialName("auth") val auth: String,
)