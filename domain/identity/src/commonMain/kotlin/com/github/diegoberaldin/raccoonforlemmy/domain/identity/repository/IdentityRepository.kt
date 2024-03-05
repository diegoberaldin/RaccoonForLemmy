package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import kotlinx.coroutines.flow.StateFlow

interface IdentityRepository {

    val authToken: StateFlow<String?>
    val isLogged: StateFlow<Boolean?>

    suspend fun startup()

    fun storeToken(
        jwt: String,
    )

    fun clearToken()

    suspend fun refreshLoggedState()
}
