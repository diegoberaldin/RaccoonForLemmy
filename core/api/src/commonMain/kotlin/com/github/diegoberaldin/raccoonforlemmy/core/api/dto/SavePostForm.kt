package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavePostForm(
    @SerialName("post_id") val postId: PostId,
    @SerialName("save") val save: Boolean,
    @SerialName("auth") val auth: String,
)
