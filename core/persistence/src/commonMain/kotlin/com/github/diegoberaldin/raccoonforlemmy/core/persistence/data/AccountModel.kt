package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data


data class AccountModel(
    val id: Long? = null,
    val username: String,
    val avatar: String? = null,
    val instance: String,
    val jwt: String,
    val active: Boolean = false,
)
