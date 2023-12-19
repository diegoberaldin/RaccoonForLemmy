package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomEmoji(
    @SerialName("id") val id: CustomEmojiId,
    @SerialName("local_site_id") val localSiteId: LocalSiteId,
    @SerialName("shortcode") val shortcode: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("alt_text") val altText: String,
    @SerialName("category") val category: String,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
)
