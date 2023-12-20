package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModRemoveCommunityView(
    @SerialName("community") val community: Community,
    @SerialName("moderator") val moderator: Person? = null,
    @SerialName("mod_remove_community") val modRemoveCommunity: ModRemoveCommunity,
)