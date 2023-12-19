package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResolvePostReportForm(
    @SerialName("report_id") val reportId: PostReportId,
    @SerialName("resolved") val resolved: Boolean,
    @SerialName("auth") val auth: String,
)
