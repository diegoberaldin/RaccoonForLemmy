package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel

interface SwitchAccountUseCase {
    suspend operator fun invoke(account: AccountModel)
}
