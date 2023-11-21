package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BanFromCommunityForm(
    @SerialName("community_id") val communityId: CommunityId,
    @SerialName("personId") val personId: PersonId,
    @SerialName("ban") val ban: Boolean,
    @SerialName("remove_data") val removeData: Boolean,
    @SerialName("reason") val reson: String?,
    @SerialName("expires") val expires: Long?,
    @SerialName("auth") val auth: String,
)
