package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class UserScoreModel(
    val postScore: Int = 0,
    val commentScore: Int = 0,
) : JavaSerializable