package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BanFromCommunityResponse(
    @SerialName("person_view") val personView: PersonView,
    @SerialName("banned") val banned: Boolean,
)
