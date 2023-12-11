package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository

internal class DefaultDeleteAccountUseCase(
    private val accountRepository: AccountRepository,
) : DeleteAccountUseCase {
    override suspend fun invoke(account: AccountModel) {
        account.id?.also { id ->
            accountRepository.delete(id)
        }
    }
}
