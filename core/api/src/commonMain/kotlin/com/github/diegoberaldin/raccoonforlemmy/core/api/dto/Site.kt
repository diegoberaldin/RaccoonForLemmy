package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Site(
    @SerialName("id") val id: SiteId,
    @SerialName("name") val name: String,
    @SerialName("sidebar") val sidebar: String? = null,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("banner") val banner: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("actor_id") val actorId: String,
    @SerialName("last_refreshed_at") val lastRefreshedAt: String,
    @SerialName("inbox_url") val inboxUrl: String,
    @SerialName("private_key") val privateKey: String? = null,
    @SerialName("public_key") val publicKey: String,
    @SerialName("instance_id") val instanceId: InstanceId,
)
