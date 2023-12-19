package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostReportResponse(
    @SerialName("post_report_view") val postReportView: PostReportView,
)
