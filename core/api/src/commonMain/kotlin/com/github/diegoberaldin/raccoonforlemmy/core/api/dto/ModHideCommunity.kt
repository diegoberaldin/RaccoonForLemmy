package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModHideCommunity(
    @SerialName("id") val id: ModlogItemId,
    @SerialName("hidden") val hidden: Boolean,
    @SerialName("reason") val reason: String? = null,
    @SerialName("when_") val date: String? = null,
)
