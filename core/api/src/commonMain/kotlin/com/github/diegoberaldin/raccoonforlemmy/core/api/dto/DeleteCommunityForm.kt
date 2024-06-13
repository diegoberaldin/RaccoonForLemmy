package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteCommunityForm(
    @SerialName("community_id") val communityId: CommunityId,
    @SerialName("deleted") val deleted: Boolean,
)
