package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

data class CommentReportModel(
    val id: Long = 0,
    val creator: UserModel? = null,
    val commentId: Long = 0,
    val postId: Long = 0,
    val originalText: String? = null,
    val reason: String? = null,
    val resolved: Boolean = false,
    val resolver: UserModel? = null,
    val publishDate: String? = null,
    val updateDate: String? = null,
)
