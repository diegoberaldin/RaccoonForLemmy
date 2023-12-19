package com.github.diegoberaldin.raccoonforlemmy.core.persistence.di

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DefaultDriverFactory
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DriverFactory
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module

actual val persistenceInnerModule = module {
    single<DriverFactory> {
        DefaultDriverFactory()
    }
}

actual fun getAccountRepository(): AccountRepository = PersistenceDiHelper.accountRepository

actual fun getSettingsRepository(): SettingsRepository = PersistenceDiHelper.settingsRepository

object PersistenceDiHelper : KoinComponent {
    val accountRepository: AccountRepository by inject()
    val settingsRepository: SettingsRepository by inject()
}