package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalSiteRateLimit(
    @SerialName("id") val id: Int? = null,
    @SerialName("local_site_id") val localSiteId: LocalSiteId,
    @SerialName("message") val message: Int,
    @SerialName("message_per_second") val messagePerSecond: Int,
    @SerialName("post") val post: Int,
    @SerialName("post_per_second") val postPerSecond: Int,
    @SerialName("register") val register: Int,
    @SerialName("register_per_second") val registerPerSecond: Int,
    @SerialName("image") val image: Int,
    @SerialName("image_per_second") val imagePerSecond: Int,
    @SerialName("comment") val comment: Int,
    @SerialName("comment_per_second") val commentPerSecond: Int,
    @SerialName("search") val search: Int,
    @SerialName("search_per_second") val searchPerSecond: Int,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
)
