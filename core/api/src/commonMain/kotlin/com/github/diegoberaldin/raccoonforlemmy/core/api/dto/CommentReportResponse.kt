package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentReportResponse(
    @SerialName("comment_report_view") val commentReportView: CommentReportView,
)
