package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPersonDetailsResponse(
    @SerialName("person_view") val personView: PersonView,
    @SerialName("comments") val comments: List<CommentView>,
    @SerialName("posts") val posts: List<PostView>,
    @SerialName("moderates") val moderates: List<CommunityModeratorView>,
)
