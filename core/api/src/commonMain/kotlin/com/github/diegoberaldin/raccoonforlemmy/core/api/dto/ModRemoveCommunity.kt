package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModRemoveCommunity(
    @SerialName("id") val id: Int,
    @SerialName("reason") val reason: String? = null,
    @SerialName("removed") val removed: Boolean,
    @SerialName("when_") val date: String? = null,
)
