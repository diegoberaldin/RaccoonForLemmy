package com.github.diegoberaldin.raccoonforlemmy.feature.settings.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutDialogMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutDialogViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsViewModel
import org.koin.dsl.module

val settingsTabModule = module {
    factory<SettingsMviModel> {
        SettingsViewModel(
            mvi = DefaultMviModel(SettingsMviModel.UiState()),
            settingsRepository = get(),
            accountRepository = get(),
            themeRepository = get(),
            languageRepository = get(),
            identityRepository = get(),
            colorSchemeProvider = get(),
            notificationCenter = get(),
            crashReportConfiguration = get(),
            crashReportSender = get(),
        )
    }
    factory<AboutDialogMviModel> {
        AboutDialogViewModel(
            mvi = DefaultMviModel(AboutDialogMviModel.UiState()),
            identityRepository = get(),
            communityRepository = get(),
        )
    }
}
