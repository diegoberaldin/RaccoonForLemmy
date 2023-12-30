package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

sealed interface SearchResultType {
    data object All : SearchResultType
    data object Posts : SearchResultType
    data object Comments : SearchResultType
    data object Users : SearchResultType
    data object Communities : SearchResultType
}
