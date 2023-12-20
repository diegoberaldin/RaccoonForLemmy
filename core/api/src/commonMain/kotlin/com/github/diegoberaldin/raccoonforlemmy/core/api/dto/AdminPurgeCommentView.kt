package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminPurgeCommentView(
    @SerialName("admin") val admin: Person? = null,
    @SerialName("admin_purge_comment") val adminPurgeComment: AdminPurgeComment,
    @SerialName("post") val post: Post,
)