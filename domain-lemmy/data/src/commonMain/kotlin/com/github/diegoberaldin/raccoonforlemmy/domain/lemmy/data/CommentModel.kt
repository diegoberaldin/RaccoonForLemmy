package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

data class CommentModel(
    val id: Int = 0,
    val postId: Int = 0,
    val text: String,
    val community: CommunityModel? = null,
    val creator: UserModel? = null,
    val score: Int = 0,
    val myVote: Int = 0,
    val saved: Boolean = false,
    val publishDate: String? = null,
    val comments: Int? = null,
)
