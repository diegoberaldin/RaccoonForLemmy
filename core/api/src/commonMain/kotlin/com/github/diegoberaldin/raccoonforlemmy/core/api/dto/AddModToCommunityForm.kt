package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddModToCommunityForm(
    @SerialName("added") val added: Boolean,
    @SerialName("person_id") val personId: PersonId,
    @SerialName("community_id") val communityId: CommunityId,
    @SerialName("auth") val auth: String,
)