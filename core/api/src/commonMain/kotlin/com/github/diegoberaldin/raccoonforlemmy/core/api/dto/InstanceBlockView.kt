package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstanceBlockView(
    @SerialName("person") val person: Person,
    @SerialName("instance") val instance: Instance,
)
