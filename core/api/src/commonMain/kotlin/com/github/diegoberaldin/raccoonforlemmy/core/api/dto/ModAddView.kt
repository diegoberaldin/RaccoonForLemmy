package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModAddView(
    @SerialName("mod_add") val modAdd: ModAdd,
    @SerialName("modded_person") val moddedPerson: Person,
    @SerialName("moderator") val moderator: Person? = null,
)