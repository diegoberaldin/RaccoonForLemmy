package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable
import kotlin.jvm.Transient

data class CommentModel(
    val id: Int = 0,
    val postId: Int = 0,
    val text: String,
    val community: CommunityModel? = null,
    val creator: UserModel? = null,
    val score: Int = 0,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val myVote: Int = 0,
    val saved: Boolean = false,
    val publishDate: String? = null,
    val comments: Int? = null,
    val path: String = "",
    @Transient
    val visible: Boolean = true,
    @Transient
    val expanded: Boolean? = null,
    @Transient
    val loadMoreButtonVisible: Boolean = false,
) : JavaSerializable {
    val depth: Int get() = (path.split(".").size - 2).coerceAtLeast(0)
    val parentId: String?
        get() = path.split(".")
            .let {
                it.getOrNull(it.lastIndex - 1)
            }?.takeIf { it != "0" }
}
