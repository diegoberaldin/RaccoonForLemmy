package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

data class UserScoreModel(
    val postScore: Int = 0,
    val commentScore: Int = 0,
)