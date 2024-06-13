package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName

enum class CommunityVisibility {
    @SerialName("Public")
    Public,

    @SerialName("LocalOnly")
    LocalOnly,
}
