package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModBanView(
    @SerialName("mod_ban") val modBan: ModBan,
    @SerialName("banned_person") val bannedPerson: Person,
    @SerialName("moderator") val moderator: Person? = null,
)