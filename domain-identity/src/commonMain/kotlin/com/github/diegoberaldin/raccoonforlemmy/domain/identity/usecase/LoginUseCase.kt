package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

interface LoginUseCase {
    suspend operator fun invoke(
        instance: String,
        username: String,
        password: String,
        totp2faToken: String? = null,
    ): Result<Unit>
}
