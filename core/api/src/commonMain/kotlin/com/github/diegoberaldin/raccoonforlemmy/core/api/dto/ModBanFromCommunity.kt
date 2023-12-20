package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModBanFromCommunity(
    @SerialName("id") val id: Int,
    @SerialName("banned") val banned: Boolean,
    @SerialName("expires") val expires: String? = null,
    @SerialName("reason") val reason: String? = null,
    @SerialName("when_") val date: String? = null,
)