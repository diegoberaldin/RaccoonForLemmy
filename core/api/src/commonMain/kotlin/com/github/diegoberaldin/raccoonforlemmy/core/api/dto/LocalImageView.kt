package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalImageView(
    @SerialName("local_image") val localImage: LocalImage,
    @SerialName("person") val person: Person,
)
