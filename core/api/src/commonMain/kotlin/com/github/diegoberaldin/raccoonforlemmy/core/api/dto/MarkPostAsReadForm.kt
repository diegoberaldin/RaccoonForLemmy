package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkPostAsReadForm(
    @SerialName("post_id")
    val postId: PostId,
    @SerialName("read")
    val read: Boolean,
    @SerialName("auth")
    val auth: String,
)
