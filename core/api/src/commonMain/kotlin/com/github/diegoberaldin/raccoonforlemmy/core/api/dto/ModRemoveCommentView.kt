package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModRemoveCommentView(
    @SerialName("comment") val comment: Comment,
    @SerialName("moderator") val moderator: Person? = null,
    @SerialName("commenter") val commenter: Person,
    @SerialName("post") val post: Post,
    @SerialName("community") val community: Community,
    @SerialName("mod_remove_comment") val modRemoveComment: ModRemoveComment,
)
