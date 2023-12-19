package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityFollowerView(
    @SerialName("community") val community: Community,
    @SerialName("follower") val follower: Person,
)
