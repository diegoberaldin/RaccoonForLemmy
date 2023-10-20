package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPostsResponse(
    @SerialName("posts") val posts: List<PostView>,
    @SerialName("next_page") val nextPage: String? = null,
)
