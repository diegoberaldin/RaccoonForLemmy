package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentForm(
    @SerialName("content") val content: String,
    @SerialName("post_id") val postId: PostId,
    @SerialName("parent_id") val parentId: CommentId? = null,
    @SerialName("language_id") val languageId: LanguageId? = null,
    @SerialName("form_id") val formId: String? = null,
    @SerialName("auth") val auth: String,
)
