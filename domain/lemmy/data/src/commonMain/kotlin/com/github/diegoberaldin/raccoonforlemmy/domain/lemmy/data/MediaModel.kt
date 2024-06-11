package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

data class MediaModel(
    val alias: String,
    val deleteToken: String,
    val date: String? = null,
)
