package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HideCommunityForm(
    @SerialName("community_id") val communityId: CommunityId,
    @SerialName("hidden") val hidden: Boolean,
    @SerialName("reason") val reason: String?,
)
