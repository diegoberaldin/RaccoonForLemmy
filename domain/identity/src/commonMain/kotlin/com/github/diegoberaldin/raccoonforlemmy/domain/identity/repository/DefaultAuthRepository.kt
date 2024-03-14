package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.LoginForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.LoginResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultAuthRepository(
    private val services: ServiceProvider,
) : AuthRepository {
    override suspend fun login(
        username: String,
        password: String,
        totp2faToken: String?,
    ): Result<LoginResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val data = LoginForm(
                username = username,
                password = password,
                totp2faToken = totp2faToken,
            )
            val response = services.auth.login(data)
            if (!response.isSuccessful) {
                val error = response.errorBody().toString()
                throw Exception(error)
            }
            response.body() ?: throw Exception("No reponse from login endpoint")
        }
    }
}
