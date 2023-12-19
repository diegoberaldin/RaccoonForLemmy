package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Language(
    @SerialName("id") val id: LanguageId,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
)
