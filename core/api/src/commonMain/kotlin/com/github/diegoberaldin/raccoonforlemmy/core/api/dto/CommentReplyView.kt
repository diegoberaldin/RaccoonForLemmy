package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentReplyView(
    @SerialName("comment_reply") val commentReply: CommentReply,
    @SerialName("comment") val comment: Comment,
    @SerialName("creator") val creator: Person,
    @SerialName("post") val post: Post,
    @SerialName("community") val community: Community,
    @SerialName("recipient") val recipient: Person,
    @SerialName("counts") val counts: CommentAggregates,
    @SerialName("creator_banned_from_community") val creatorBannedFromCommunity: Boolean,
    @SerialName("subscribed") val subscribed: SubscribedType,
    @SerialName("saved") val saved: Boolean,
    @SerialName("creator_blocked") val creatorBlocked: Boolean,
    @SerialName("my_vote") val myVote: Int? = null,
)