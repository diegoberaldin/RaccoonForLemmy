package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

sealed interface SearchResultType {
    object All : SearchResultType
    object Posts : SearchResultType
    object Comments : SearchResultType
    object Users : SearchResultType
    object Communities : SearchResultType
}