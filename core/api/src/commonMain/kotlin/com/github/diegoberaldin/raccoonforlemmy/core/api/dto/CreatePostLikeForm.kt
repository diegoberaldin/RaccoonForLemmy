package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostLikeForm(
    @SerialName("post_id") val postId: PostId,
    @SerialName("score") val score: Int,
    @SerialName("auth") val auth: String,
)
