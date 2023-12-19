package com.github.diegoberaldin.raccoonforlemmy.feature.settings.di

import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getSettingsViewModel(): SettingsMviModel = SettingsScreenModelHelper.settingsViewModel

object SettingsScreenModelHelper : KoinComponent {
    val settingsViewModel: SettingsMviModel by inject()
}
