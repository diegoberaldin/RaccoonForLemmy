package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockCommunityForm(
    @SerialName("community_id") val communityId: CommunityId,
    @SerialName("block") val block: Boolean,
    @SerialName("auth") val auth: String,
)
