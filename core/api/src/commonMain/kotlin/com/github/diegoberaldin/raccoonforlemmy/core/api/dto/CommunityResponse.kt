package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityResponse(
    @SerialName("community_view") val communityView: CommunityView,
    @SerialName("discussion_languages") val discussionLanguages: List<LanguageId>,
)
