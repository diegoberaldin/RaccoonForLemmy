package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PictrsImages(
    @SerialName("msg") val msg: String,
    @SerialName("files") val files: List<PictrsImage>?,
)
