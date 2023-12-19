package com.github.diegoberaldin.raccoonforlemmy.core.preferences.di

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.DefaultTemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module

actual val corePreferencesModule = module {
    single<Settings> {
        KeychainSettings(service = "secret_shared_prefs")
    }
    single<TemporaryKeyStore> {
        DefaultTemporaryKeyStore(settings = get())
    }
}

object TemporaryKeyStoreHelper : KoinComponent {
    val temporaryKeyStore: TemporaryKeyStore by inject()
}
