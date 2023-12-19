package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class MetadataModel(
    val title: String = "",
    val description: String = "",
) : JavaSerializable
