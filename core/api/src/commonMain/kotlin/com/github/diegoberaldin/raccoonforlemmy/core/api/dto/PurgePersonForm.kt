package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurgePersonForm(
    @SerialName("person_id") val personId: PersonId,
    @SerialName("reason") val reason: String?,
)
