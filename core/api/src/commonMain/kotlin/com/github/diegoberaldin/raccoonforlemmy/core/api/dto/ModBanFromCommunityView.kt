package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModBanFromCommunityView(
    @SerialName("mod_ban_from_community") val modBanFromCommunity: ModBanFromCommunity,
    @SerialName("banned_person") val bannedPerson: Person,
    @SerialName("moderator") val moderator: Person? = null,
    @SerialName("community") val community: Community,
)
