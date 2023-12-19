package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentReport(
    @SerialName("id") val id: CommentReportId,
    @SerialName("creator_id") val creatorId: PersonId,
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("original_comment_text") val originalCommentText: String,
    @SerialName("reason") val reason: String,
    @SerialName("resolved") val resolved: Boolean,
    @SerialName("resolver_id") val resolverId: PersonId? = null,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
)
