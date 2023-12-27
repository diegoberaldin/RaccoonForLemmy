package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModAddCommunityView(
    @SerialName("community") val community: Community,
    @SerialName("mod_add_community") val modAddCommunity: ModAddCommunity,
    @SerialName("modded_person") val moddedPerson: Person,
    @SerialName("moderator") val moderator: Person? = null,
)
