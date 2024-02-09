package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import kotlinx.coroutines.flow.StateFlow

interface IdentityRepository {

    val authToken: StateFlow<String?>
    val isLogged: StateFlow<Boolean?>

    fun storeToken(
        jwt: String,
    )

    fun clearToken()
}
