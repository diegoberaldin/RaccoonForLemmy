package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostReportForm(
    @SerialName("post_id") val postId: PostId,
    @SerialName("reason") val reason: String,
    @SerialName("auth") val auth: String,
)
