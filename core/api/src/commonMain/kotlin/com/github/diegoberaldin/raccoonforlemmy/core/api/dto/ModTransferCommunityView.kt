package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModTransferCommunityView(
    @SerialName("community") val community: Community,
    @SerialName("moderator") val moderator: Person? = null,
    @SerialName("modded_person") val moddedPerson: Person,
    @SerialName("mod_transfer_community") val modTransferCommunity: ModTransferCommunity,
)
