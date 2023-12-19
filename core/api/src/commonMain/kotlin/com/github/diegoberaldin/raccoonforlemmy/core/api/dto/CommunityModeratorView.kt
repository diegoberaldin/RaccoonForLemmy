package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityModeratorView(
    @SerialName("community") val community: Community,
    @SerialName("moderator") val moderator: Person,
)
