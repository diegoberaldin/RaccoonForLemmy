package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    @SerialName("id") val id: PersonId,
    @SerialName("name") val name: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar") val avatar: String? = null,
    @SerialName("banned") val banned: Boolean,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
    @SerialName("actor_id") val actorId: String,
    @SerialName("bio") val bio: String? = null,
    @SerialName("local") val local: Boolean,
    @SerialName("banner") val banner: String? = null,
    @SerialName("deleted") val deleted: Boolean,
    @SerialName("matrix_user_id") val matrixUserId: String? = null,
    @SerialName("admin") val admin: Boolean? = null,
    @SerialName("bot_account") val botAccount: Boolean,
    @SerialName("ban_expires") val banExpires: String? = null,
    @SerialName("instance_id") val instanceId: InstanceId,
)
