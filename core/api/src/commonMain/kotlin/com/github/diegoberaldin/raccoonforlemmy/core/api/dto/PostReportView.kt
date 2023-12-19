package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostReportView(
    @SerialName("post_report") val postReport: PostReport,
    @SerialName("post") val post: Post,
    @SerialName("community") val community: Community,
    @SerialName("creator") val creator: Person,
    @SerialName("post_creator") val postCreator: Person,
    @SerialName("creator_banned_from_community") val creatorBannedFromCommunity: Boolean,
    @SerialName("my_vote") val myVote: Int? = null,
    @SerialName("counts") val counts: PostAggregates,
    @SerialName("resolver") val resolver: Person? = null,
)
