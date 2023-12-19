package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentReportView(
    @SerialName("comment_report") val commentReport: CommentReport,
    @SerialName("comment") val comment: Comment,
    @SerialName("post") val post: Post,
    @SerialName("community") val community: Community,
    @SerialName("creator") val creator: Person,
    @SerialName("comment_creator") val commentCreator: Person,
    @SerialName("counts") val counts: CommentAggregates,
    @SerialName("creator_banned_from_community") val creatorBannedFromCommunity: Boolean,
    @SerialName("my_vote") val myVote: Int? = null,
    @SerialName("resolver") val resolver: Person? = null,
)
