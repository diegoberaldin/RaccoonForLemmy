package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

sealed interface SearchResult : JavaSerializable {
    data class Post(val model: PostModel) : SearchResult
    data class Comment(val model: CommentModel) : SearchResult
    data class User(val model: UserModel) : SearchResult
    data class Community(val model: CommunityModel) : SearchResult
}