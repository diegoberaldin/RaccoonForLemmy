package com.github.diegoberaldin.raccoonforlemmy.feature.settings.di

import com.github.diegoberaldin.raccoonforlemmy.feature.settings.advanced.AdvancedSettingsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.advanced.AdvancedSettingsViewModel
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
            settingsRepository = get(),
            accountRepository = get(),
            themeRepository = get(),
            l10nManager = get(),
            identityRepository = get(),
            notificationCenter = get(),
            crashReportConfiguration = get(),
            getSortTypesUseCase = get(),
        )
    }
    factory<SettingsColorAndFontMviModel> {
        SettingsColorAndFontViewModel(
            settingsRepository = get(),
            accountRepository = get(),
            themeRepository = get(),
            identityRepository = get(),
            colorSchemeProvider = get(),
            notificationCenter = get(),
        )
    }
    factory<AdvancedSettingsMviModel> {
        AdvancedSettingsViewModel(
            settingsRepository = get(),
            accountRepository = get(),
            themeRepository = get(),
            identityRepository = get(),
            notificationCenter = get(),
            galleryHelper = get(),
        )
    }
}
