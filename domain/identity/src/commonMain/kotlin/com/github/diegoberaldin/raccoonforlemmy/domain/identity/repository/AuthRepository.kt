package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.LoginResponse

interface AuthRepository {
    suspend fun login(
        username: String,
        password: String,
        totp2faToken: String? = null,
    ): Result<LoginResponse>
}
