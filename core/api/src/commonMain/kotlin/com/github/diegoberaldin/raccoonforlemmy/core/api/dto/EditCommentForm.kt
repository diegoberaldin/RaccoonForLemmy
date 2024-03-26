package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditCommentForm(
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("content") val content: String? = null,
    @SerialName("language_id") val languageId: LanguageId? = null,
    @SerialName("form_id") val formId: String? = null,
    @SerialName("auth") val auth: String,
)
