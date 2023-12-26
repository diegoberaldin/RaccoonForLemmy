package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class CommentReportModel(
    val id: Int = 0,
    val creator: UserModel? = null,
    val commentId: Int = 0,
    val postId: Int = 0,
    val originalText: String? = null,
    val reason: String? = null,
    val resolved: Boolean = false,
    val resolver: UserModel? = null,
    val publishDate: String? = null,
    val updateDate: String? = null,
) : JavaSerializable
