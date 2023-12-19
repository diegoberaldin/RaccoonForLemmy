package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonView(
    @SerialName("person") val person: Person,
    @SerialName("counts") val counts: PersonAggregates,
)
