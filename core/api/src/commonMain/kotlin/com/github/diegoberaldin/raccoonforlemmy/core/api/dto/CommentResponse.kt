package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentResponse(
    @SerialName("comment_view") val commentView: CommentView,
    @SerialName("recipient_ids") val recipientIds: List<LocalUserId>,
    @SerialName("form_id") val formId: String? = null,
)
