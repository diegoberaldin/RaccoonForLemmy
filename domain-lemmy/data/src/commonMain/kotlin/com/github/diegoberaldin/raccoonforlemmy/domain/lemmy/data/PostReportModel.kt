package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

data class PostReportModel(
    val id: Int = 0,
    val creator: UserModel? = null,
    val postId: Int = 0,
    val reason: String? = null,
    val originalTitle: String? = null,
    val originalText: String? = null,
    val originalUrl: String? = null,
    val resolved: Boolean = false,
    val resolver: UserModel? = null,
    val publishDate: String? = null,
    val updateDate: String? = null,
)
