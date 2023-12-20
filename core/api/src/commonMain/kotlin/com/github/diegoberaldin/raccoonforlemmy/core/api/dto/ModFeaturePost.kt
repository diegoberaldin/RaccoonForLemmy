package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModFeaturePost(
    @SerialName("id") val id: Int,
    @SerialName("featured") val featured: Boolean,
    @SerialName("is_featured_community") val isFeaturedCommunity: Boolean,
    @SerialName("when_") val date: String? = null,
)