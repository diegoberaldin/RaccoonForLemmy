package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName

enum class CommentSortType {
    @SerialName("Hot")
    Hot,

    @SerialName("Top")
    Top,

    @SerialName("New")
    New,

    @SerialName("Old")
    Old,
}
