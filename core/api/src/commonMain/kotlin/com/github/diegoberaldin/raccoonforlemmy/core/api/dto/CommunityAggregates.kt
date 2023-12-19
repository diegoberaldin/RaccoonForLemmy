package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityAggregates(
    @SerialName("id") val id: Int? = null,
    @SerialName("community_id") val communityId: CommunityId,
    @SerialName("subscribers") val subscribers: Int,
    @SerialName("posts") val posts: Int,
    @SerialName("comments") val comments: Int,
    @SerialName("published") val published: String,
    @SerialName("users_active_day") val usersActiveDay: Int,
    @SerialName("users_active_week") val usersActiveWeek: Int,
    @SerialName("users_active_month") val usersActiveMonth: Int,
    @SerialName("users_active_half_year") val usersActiveHalfYear: Int,
    @SerialName("hot_rank") val hotRank: Float? = null,
)
