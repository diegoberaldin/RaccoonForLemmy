package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCommunityForm(
    @SerialName("name") val name: String,
    @SerialName("icon") val icon: String? = null,
    @SerialName("banner") val banner: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("nsfw") val nsfw: Boolean? = null,
    @SerialName("posting_restricted_to_mods") val postingRestrictedToMods: Boolean? = null,
    @SerialName("local_only") val localOnly: Boolean? = null,
    @SerialName("discussion_languages") val discussionLanguages: List<LanguageId>? = null,
)
