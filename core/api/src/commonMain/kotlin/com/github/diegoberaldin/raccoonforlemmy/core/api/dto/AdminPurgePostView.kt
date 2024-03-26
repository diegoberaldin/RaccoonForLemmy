package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminPurgePostView(
    @SerialName("admin") val admin: Person? = null,
    @SerialName("admin_purge_post") val adminPurgePost: AdminPurgePost,
    @SerialName("community") val community: Community,
)
