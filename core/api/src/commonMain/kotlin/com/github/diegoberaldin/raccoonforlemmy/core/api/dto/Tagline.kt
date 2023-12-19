package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tagline(
    @SerialName("id") val id: Int,
    @SerialName("local_site_id") val localSiteId: LocalSiteId,
    @SerialName("content") val content: String,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
)
