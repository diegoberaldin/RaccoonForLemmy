package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    @SerialName("id") val id: CommentId,
    @SerialName("creator_id") val creatorId: PersonId,
    @SerialName("post_id") val postId: PostId,
    @SerialName("content") val content: String,
    @SerialName("removed") val removed: Boolean,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
    @SerialName("deleted") val deleted: Boolean,
    @SerialName("ap_id") val apId: String,
    @SerialName("local") val local: Boolean,
    @SerialName("path") val path: String,
    @SerialName("distinguished") val distinguished: Boolean,
    @SerialName("language_id") val languageId: LanguageId,
)
