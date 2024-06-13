package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditCommunityForm(
    @SerialName("community_id") val communityId: CommunityId? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("banner") val banner: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("nsfw") val nsfw: Boolean? = null,
    @SerialName("posting_restricted_to_mods") val postingRestrictedToMods: Boolean? = null,
    @SerialName("local_only") val localOnly: Boolean? = null,
    @SerialName("discussion_languages") val discussionLanguages: List<LanguageId>? = null,
    @SerialName("visibility") val visibility: CommunityVisibility? = null,
)
