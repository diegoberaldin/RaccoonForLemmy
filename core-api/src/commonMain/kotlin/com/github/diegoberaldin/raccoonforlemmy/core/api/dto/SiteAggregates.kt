package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SiteAggregates(
    @SerialName("id") val id: Int? = null,
    @SerialName("site_id") val siteOd: SiteId,
    @SerialName("users") val users: Int,
    @SerialName("posts") val posts: Int,
    @SerialName("comments") val comments: Int,
    @SerialName("communities") val communities: Int,
    @SerialName("users_active_day") val usersActiveDay: Int,
    @SerialName("users_active_week") val usersActiveWeek: Int,
    @SerialName("users_active_month") val usersActiveMonth: Int,
    @SerialName("users_active_half_year") val usersActiveHalfYear: Int,
)
