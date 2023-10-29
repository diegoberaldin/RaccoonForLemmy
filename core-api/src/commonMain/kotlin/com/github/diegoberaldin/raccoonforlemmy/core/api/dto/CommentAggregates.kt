package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentAggregates(
    @SerialName("id") val id: Int,
    @SerialName("comment_id") val commentId: CommentId,
    @SerialName("score") val score: Int,
    @SerialName("upvotes") val upvotes: Int,
    @SerialName("downvotes") val downvotes: Int,
    @SerialName("published") val published: String,
    @SerialName("child_count") val childCount: Int,
    @SerialName("hot_rank") val hotRank: Float? = null,
)
