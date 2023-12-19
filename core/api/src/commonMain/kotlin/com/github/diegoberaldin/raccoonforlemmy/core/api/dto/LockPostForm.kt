package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LockPostForm(
    @SerialName("post_id") val postId: PostId,
    @SerialName("locked") val locked: Boolean,
    @SerialName("auth") val auth: String,
)
