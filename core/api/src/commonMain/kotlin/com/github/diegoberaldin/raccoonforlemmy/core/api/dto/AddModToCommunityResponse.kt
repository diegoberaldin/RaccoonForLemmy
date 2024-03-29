package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddModToCommunityResponse(
    @SerialName("moderators") val moderators: List<CommunityModeratorView>? = null,
)
