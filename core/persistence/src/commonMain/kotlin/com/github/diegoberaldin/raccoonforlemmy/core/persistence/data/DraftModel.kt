package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data

sealed interface DraftType {
    data object Post : DraftType

    data object Comment : DraftType
}

data class DraftModel(
    val id: Long? = null,
    val type: DraftType,
    val body: String,
    val communityId: Long? = null,
    val languageId: Long? = null,
    val postId: Long? = null,
    val parentId: Long? = null,
    val title: String? = null,
    val url: String? = null,
    val nsfw: Boolean? = null,
    val date: Long? = null,
    val reference: String? = null,
)
