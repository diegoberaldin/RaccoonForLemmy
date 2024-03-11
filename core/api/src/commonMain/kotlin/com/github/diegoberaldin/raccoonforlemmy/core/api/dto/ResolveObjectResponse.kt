package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResolveObjectResponse(
    @SerialName("comment") val comment: CommentView? = null,
    @SerialName("post") val post: PostView? = null,
    @SerialName("community") val community: CommunityView? = null,
    @SerialName("person") val user: PersonView? = null,
)
