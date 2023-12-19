package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

sealed interface SearchResultType {
    data object All : SearchResultType
    data object Posts : SearchResultType
    data object Comments : SearchResultType
    data object Users : SearchResultType
    data object Communities : SearchResultType
}

sealed interface SearchResult {
    data class Post(val model: PostModel) : SearchResult
    data class Comment(val model: CommentModel) : SearchResult
    data class User(val model: UserModel) : SearchResult
    data class Community(val model: CommunityModel) : SearchResult
}