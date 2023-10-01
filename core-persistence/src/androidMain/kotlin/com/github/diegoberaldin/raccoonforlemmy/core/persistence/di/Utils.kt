package com.github.diegoberaldin.raccoonforlemmy.core.persistence.di

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseKeyProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DefaultDatabaseKeyProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DefaultDriverFactory
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DriverFactory
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent

actual val persistenceInnerModule = module {
    single<DriverFactory> {
        DefaultDriverFactory(
            context = get(),
            keyProvider = get(),
        )
    }
    single<DatabaseKeyProvider> {
        DefaultDatabaseKeyProvider(
            keyStore = get(),
        )
    }
}

actual fun getAccountRepository(): AccountRepository {
    val res: AccountRepository by KoinJavaComponent.inject(AccountRepository::class.java)
    return res
}

actual fun getSettingsRepository(): SettingsRepository {
    val res: SettingsRepository by KoinJavaComponent.inject(SettingsRepository::class.java)
    return res
}