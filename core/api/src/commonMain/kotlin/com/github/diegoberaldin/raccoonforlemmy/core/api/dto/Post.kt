package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    @SerialName("id") val id: PostId,
    @SerialName("name") val name: String,
    @SerialName("url") val url: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("creator_id") val creatorId: PersonId,
    @SerialName("community_id") val communityId: CommunityId,
    @SerialName("removed") val removed: Boolean,
    @SerialName("locked") val locked: Boolean,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
    @SerialName("deleted") val deleted: Boolean,
    @SerialName("nsfw") val nsfw: Boolean,
    @SerialName("embed_title") val embedTitle: String? = null,
    @SerialName("embed_description") val embedDescription: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("ap_id") val apId: String,
    @SerialName("local") val local: Boolean,
    @SerialName("embed_video_url") val embedVideoUrl: String? = null,
    @SerialName("language_id") val languageId: LanguageId,
    @SerialName("featured_community") val featuredCommunity: Boolean,
    @SerialName("featured_local") val featuredLocal: Boolean,
)
