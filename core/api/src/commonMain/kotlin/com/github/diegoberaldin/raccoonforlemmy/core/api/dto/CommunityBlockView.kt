package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityBlockView(
    @SerialName("person") val person: Person,
    @SerialName("community") val community: Community,
)
