package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModLockPostView(
    @SerialName("community") val community: Community,
    @SerialName("moderator") val moderator: Person? = null,
    @SerialName("post") val post: Post,
    @SerialName("mod_lock_post") val modLockPost: ModLockPost,
)
