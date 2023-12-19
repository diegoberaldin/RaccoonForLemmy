package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName

enum class SearchType {
    @SerialName("All")
    All,

    @SerialName("Comments")
    Comments,

    @SerialName("Posts")
    Posts,

    @SerialName("Communities")
    Communities,

    @SerialName("Users")
    Users,

    @SerialName("Url")
    Url,
}
