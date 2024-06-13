package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalImage(
    @SerialName("local_user_id") val localUserId: Long? = null,
    @SerialName("pictrs_alias") val pictrsAlias: String,
    @SerialName("pictrs_delete_token") val pictrsDeleteToken: String,
    @SerialName("published") val published: String,
)
