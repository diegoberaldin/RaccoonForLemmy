package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

data class PrivateMessageModel(
    val id: Int = 0,
    val content: String? = null,
    val creator: UserModel? = null,
    val recipient: UserModel? = null,
    val publishDate: String? = null,
    val updateDate: String? = null,
    val read: Boolean = false,
)