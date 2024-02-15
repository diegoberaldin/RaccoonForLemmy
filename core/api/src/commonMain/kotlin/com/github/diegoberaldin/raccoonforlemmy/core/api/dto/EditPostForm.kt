package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditPostForm(
    @SerialName("post_id") val postId: PostId,
    @SerialName("name") val name: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("nsfw") val nsfw: Boolean? = null,
    @SerialName("language_id") val languageId: LanguageId? = null,
    @SerialName("auth") val auth: String,
)
