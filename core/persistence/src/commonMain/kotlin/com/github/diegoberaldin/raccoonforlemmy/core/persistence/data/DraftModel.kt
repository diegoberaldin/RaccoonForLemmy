package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data

sealed interface DraftType {
    data object Post : DraftType

    data object Comment : DraftType
}

data class DraftModel(
    val id: Long? = null,
    val type: DraftType,
    val body: String,
    val communityId: Int? = null,
    val languageId: Int? = null,
    val postId: Int? = null,
    val parentId: Int? = null,
    val title: String? = null,
    val url: String? = null,
    val nsfw: Boolean? = null,
    val date: Long? = null,
    val reference: String? = null,
)
