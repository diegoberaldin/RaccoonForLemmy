package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

sealed interface SearchResultType : JavaSerializable {
    data object All : SearchResultType
    data object Posts : SearchResultType
    data object Comments : SearchResultType
    data object Users : SearchResultType
    data object Communities : SearchResultType
}
