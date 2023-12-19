package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockCommunityResponse(
    @SerialName("community_view") val communityView: CommunityView,
    @SerialName("blocked") val blocked: Boolean,
)
