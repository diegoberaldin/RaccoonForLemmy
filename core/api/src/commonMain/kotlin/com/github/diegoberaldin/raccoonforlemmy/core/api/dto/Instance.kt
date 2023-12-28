package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Instance(
    @SerialName("id") val id: InstanceId,
    @SerialName("domain") val domain: String,
    @SerialName("published") val published: String,
    @SerialName("software") val software: String? = null,
    @SerialName("updated") val updated: String? = null,
    @SerialName("version") val version: String? = null,
)
