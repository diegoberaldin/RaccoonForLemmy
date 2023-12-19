package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListCommentReportsResponse(
    @SerialName("comment_reports") val commentReports: List<CommentReportView>,
)
