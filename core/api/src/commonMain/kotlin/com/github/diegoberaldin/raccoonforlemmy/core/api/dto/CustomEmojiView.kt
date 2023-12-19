package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomEmojiView(
    @SerialName("custom_emoji") val customEmoji: CustomEmoji,
    @SerialName("keywords") val keywords: List<CustomEmojiKeyword>,
)
