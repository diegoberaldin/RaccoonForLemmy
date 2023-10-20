package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class UserModel(
    val id: Int = 0,
    val instanceId: Int = 0,
    val name: String = "",
    val displayName: String = "",
    val avatar: String? = null,
    val banner: String? = null,
    val host: String = "",
    val score: UserScoreModel? = null,
    val accountAge: String = "",
) : JavaSerializable
