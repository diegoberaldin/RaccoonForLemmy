package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModFeaturePostView(
    @SerialName("community") val community: Community,
    @SerialName("mod_feature_post") val modFeaturePost: ModFeaturePost,
    @SerialName("moderator") val moderator: Person? = null,
    @SerialName("post") val post: Post,
)