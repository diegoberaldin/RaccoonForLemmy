package com.github.diegoberaldin.raccoonforlemmy.core.preferences.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.DefaultTemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val corePreferencesModule = module {
    single { params ->
        SharedPreferencesProvider(
            name = params[0],
            context = get(),
        )
    }
    single<Settings> { params ->
        val sharedPreferencesProvider: SharedPreferencesProvider = get(parameters = {
            parametersOf(params[0])
        })
        SharedPreferencesSettings(
            delegate = sharedPreferencesProvider.sharedPreferences,
            commit = false,
        )
    }
    single<TemporaryKeyStore>(named("default")) {
        DefaultTemporaryKeyStore(settings = get(parameters = { parametersOf(null) }))
    }
    factory<TemporaryKeyStore>(named("custom")) { params ->
        DefaultTemporaryKeyStore(settings = get(parameters = { parametersOf(params[0]) }))
    }
}

private class SharedPreferencesProvider(
    name: String? = null,
    context: Context,
) {

    companion object {
        const val PREFERENCES_NAME = "secret_shared_prefs"
    }

    private val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        name ?: PREFERENCES_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}
