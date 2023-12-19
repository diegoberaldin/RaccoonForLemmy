package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkAllAsReadForm(
    @SerialName("auth")
    val auth: String,
)
