package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeaturePostForm(
    @SerialName("post_id") val postId: PostId,
    @SerialName("auth") val auth: String,
    @SerialName("featured") val featured: Boolean,
    @SerialName("feature_type") val featureType: PostFeatureType,
)
