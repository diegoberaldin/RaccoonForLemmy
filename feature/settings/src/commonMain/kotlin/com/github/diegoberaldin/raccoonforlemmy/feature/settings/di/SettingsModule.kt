package com.github.diegoberaldin.raccoonforlemmy.feature.settings.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.colors.SettingsColorAndFontMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.colors.SettingsColorAndFontViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.about.di.aboutModule
import org.koin.dsl.module

val settingsTabModule = module {
    includes(aboutModule)
    factory<SettingsMviModel> {
        SettingsViewModel(
            mvi = DefaultMviModel(SettingsMviModel.UiState()),
            settingsRepository = get(),
            accountRepository = get(),
            themeRepository = get(),
            languageRepository = get(),
            identityRepository = get(),
            notificationCenter = get(),
            crashReportConfiguration = get(),
            crashReportSender = get(),
            contentResetCoordinator = get(),
            getSortTypesUseCase = get(),
        )
    }
    factory<SettingsColorAndFontMviModel> {
        SettingsColorAndFontViewModel(
            mvi = DefaultMviModel(SettingsColorAndFontMviModel.UiState()),
            settingsRepository = get(),
            accountRepository = get(),
            themeRepository = get(),
            identityRepository = get(),
            colorSchemeProvider = get(),
            notificationCenter = get(),
        )
    }
}
