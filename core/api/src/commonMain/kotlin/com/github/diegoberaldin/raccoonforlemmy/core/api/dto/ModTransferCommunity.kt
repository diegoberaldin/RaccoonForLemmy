package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModTransferCommunity(
    @SerialName("id") val id: Int,
    @SerialName("when_") val date: String? = null,
)
