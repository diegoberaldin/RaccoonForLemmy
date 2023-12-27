package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModHideCommunityView(
    @SerialName("admin") val admin: Person? = null,
    @SerialName("community") val community: Community,
    @SerialName("mod_hide_community") val modHideCommunity: ModHideCommunity,
)
