package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable
import com.github.diegoberaldin.raccoonforlemmy.core.utils.looksLikeAnImage

data class PostModel(
    val id: Int = 0,
    val originalUrl: String? = null,
    val title: String = "",
    val text: String = "",
    val score: Int = 0,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val comments: Int = 0,
    val thumbnailUrl: String? = null,
    val url: String? = null,
    val embedVideoUrl: String? = null,
    val community: CommunityModel? = null,
    val creator: UserModel? = null,
    val saved: Boolean = false,
    val myVote: Int = 0,
    val publishDate: String? = null,
    val updateDate: String? = null,
    val nsfw: Boolean = false,
    val read: Boolean = false,
    val crossPosts: List<PostModel> = emptyList(),
    val featuredCommunity: Boolean = false,
    val removed: Boolean = false,
    val locked: Boolean = false,
) : JavaSerializable

val PostModel.imageUrl: String
    get() = url?.takeIf { it.looksLikeAnImage }?.takeIf { it.isNotEmpty() } ?: run {
        thumbnailUrl
    }.orEmpty()
