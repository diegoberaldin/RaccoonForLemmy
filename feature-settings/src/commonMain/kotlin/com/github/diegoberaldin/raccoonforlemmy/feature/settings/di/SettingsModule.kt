package com.github.diegoberaldin.raccoonforlemmy.feature.settings.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.content.SettingsScreenMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.content.SettingsScreenViewModel
import org.koin.dsl.module

val settingsTabModule = module {
    factory {
        SettingsScreenViewModel(
            mvi = DefaultMviModel(
                SettingsScreenMviModel.UiState(),
            ),
            settingsRepository = get(),
            accountRepository = get(),
            themeRepository = get(),
            languageRepository = get(),
            identityRepository = get(),
            colorSchemeProvider = get(),
            notificationCenter = get(),
        )
    }
}
