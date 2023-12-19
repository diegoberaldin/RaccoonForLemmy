package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPostResponse(
    @SerialName("post_view") val postView: PostView,
    @SerialName("community_view") val communityView: CommunityView,
    @SerialName("moderators") val moderators: List<CommunityModeratorView>,
    @SerialName("cross_posts") val crossPosts: List<PostView>,
)
