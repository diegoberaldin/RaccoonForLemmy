package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class InstanceModel(
    val id: Int = 0,
    val domain: String = "",
) : JavaSerializable