package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteImageForm(
    @SerialName("filename") val filename: String,
    @SerialName("token") val token: String,
)
