package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

sealed interface SearchResult {
    data class Post(val model: PostModel) : SearchResult

    data class Comment(val model: CommentModel) : SearchResult

    data class User(val model: UserModel) : SearchResult

    data class Community(val model: CommunityModel) : SearchResult
}
