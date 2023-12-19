package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetCommunityResponse(
    @SerialName("community_view") val communityView: CommunityView,
    @SerialName("site") val site: Site? = null,
    @SerialName("moderators") val moderators: List<CommunityModeratorView>,
    @SerialName("discussion_languages") val discussionLanguages: List<LanguageId>,
)
