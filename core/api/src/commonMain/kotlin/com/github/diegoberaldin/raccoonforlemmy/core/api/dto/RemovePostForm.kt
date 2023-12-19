package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemovePostForm(
    @SerialName("post_id") val postId: PostId,
    @SerialName("reason") val reason: String?,
    @SerialName("removed") val removed: Boolean,
    @SerialName("auth") val auth: String,
)
