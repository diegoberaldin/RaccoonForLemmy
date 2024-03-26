package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminPurgePersonView(
    @SerialName("admin") val admin: Person? = null,
    @SerialName("admin_purge_person") val adminPurgePerson: AdminPurgePerson,
)
