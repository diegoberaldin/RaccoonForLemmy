package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostForm(
    @SerialName("name") val name: String,
    @SerialName("community_id") val communityId: CommunityId,
    @SerialName("url") val url: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("honeypot") val honeypot: String? = null,
    @SerialName("nsfw") val nsfw: Boolean? = null,
    @SerialName("language_id") val languageId: LanguageId? = null,
    @SerialName("auth") val auth: String,
)
