package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("jwt") val token: String? = null,
    @SerialName("registration_created") val registrationCreated: Boolean? = null,
    @SerialName("verify_email_sent") val verifyEmailSent: Boolean? = null,
    @SerialName("error") var error: String? = null,
)
