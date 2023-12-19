package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostReport(
    @SerialName("id") val id: PostReportId,
    @SerialName("creator_id") val creatorId: PersonId,
    @SerialName("post_id") val postId: PostId,
    @SerialName("original_post_name") val originalPostName: String,
    @SerialName("original_post_url") val originalPostUrl: String? = null,
    @SerialName("original_post_body") val originalPostBody: String? = null,
    @SerialName("reason") val reason: String,
    @SerialName("resolved") val resolved: Boolean,
    @SerialName("resolver_id") val resolverId: PersonId? = null,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
)
