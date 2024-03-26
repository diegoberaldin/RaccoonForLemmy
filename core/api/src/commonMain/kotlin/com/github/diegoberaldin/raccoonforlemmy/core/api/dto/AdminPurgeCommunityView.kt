package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminPurgeCommunityView(
    @SerialName("admin") val admin: Person? = null,
    @SerialName("admin_purge_community") val adminPurgeCommunity: AdminPurgeCommunity,
)
