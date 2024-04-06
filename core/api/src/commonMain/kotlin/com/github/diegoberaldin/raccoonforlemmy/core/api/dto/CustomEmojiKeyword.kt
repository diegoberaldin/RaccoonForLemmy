package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomEmojiKeyword(
    @SerialName("id") val id: Long? = null,
    @SerialName("custom_emoji_id") val customEmojiId: CustomEmojiId,
    @SerialName("keyword") val keyword: String,
)
