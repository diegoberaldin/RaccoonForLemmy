package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostView(
    @SerialName("post") val post: Post,
    @SerialName("creator") val creator: Person,
    @SerialName("community") val community: Community,
    @SerialName("creator_banned_from_community") val creatorBannedFromCommunity: Boolean,
    @SerialName("counts") val counts: PostAggregates,
    @SerialName("subscribed") val subscribed: SubscribedType,
    @SerialName("saved") val saved: Boolean,
    @SerialName("read") val read: Boolean,
    @SerialName("creator_blocked") val creatorBlocked: Boolean,
    @SerialName("my_vote") val myVote: Int? = null,
    @SerialName("unread_comments") val unreadComments: Int? = null,
)
