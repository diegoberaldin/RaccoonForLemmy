package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    @SerialName("actor_id") val actorId: String,
    @SerialName("avatar") val avatar: String? = null,
    @SerialName("ban_expires") val banExpires: String? = null,
    @SerialName("banned") val banned: Boolean,
    @SerialName("banner") val banner: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("bot_account") val botAccount: Boolean? = null,
    @SerialName("deleted") val deleted: Boolean,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("id") val id: PersonId,
    @SerialName("instance_id") val instanceId: InstanceId,
    @SerialName("local") val local: Boolean,
    @SerialName("matrix_user_id") val matrixUserId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
)
