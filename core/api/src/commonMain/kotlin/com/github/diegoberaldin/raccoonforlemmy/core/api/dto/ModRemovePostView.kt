package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModRemovePostView(
    @SerialName("community") val community: Community,
    @SerialName("moderator") val moderator: Person? = null,
    @SerialName("post") val post: Post,
    @SerialName("mod_remove_post") val modRemovePost: ModRemovePost,
)
