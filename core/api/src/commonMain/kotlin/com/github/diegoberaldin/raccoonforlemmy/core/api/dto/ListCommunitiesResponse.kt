package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListCommunitiesResponse(
    @SerialName("communities")
    val communities: List<CommunityView>,
)
