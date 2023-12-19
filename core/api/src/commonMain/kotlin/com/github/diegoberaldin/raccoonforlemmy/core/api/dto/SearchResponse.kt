package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    @SerialName("type_") val type: SearchType,
    @SerialName("comments") val comments: List<CommentView>,
    @SerialName("posts") val posts: List<PostView>,
    @SerialName("communities") val communities: List<CommunityView>,
    @SerialName("users") val users: List<PersonView>,
)
