package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import kotlinx.coroutines.flow.StateFlow

interface IdentityRepository {

    val authToken: StateFlow<String?>
    val isLogged: StateFlow<Boolean?>
    val cachedUser: UserModel?

    suspend fun startup()

    fun storeToken(jwt: String)

    fun clearToken()

    suspend fun refreshLoggedState()
}
