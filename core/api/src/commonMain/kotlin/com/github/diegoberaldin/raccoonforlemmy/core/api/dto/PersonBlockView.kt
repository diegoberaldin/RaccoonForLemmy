package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonBlockView(
    @SerialName("person") val person: Person,
    @SerialName("target") val target: Person,
)
