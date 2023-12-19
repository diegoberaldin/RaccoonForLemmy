package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginForm(
    @SerialName("username_or_email") val username: String,
    @SerialName("password") val password: String,
    @SerialName("totp_2fa_token") val totp2faToken: String? = null,
)
