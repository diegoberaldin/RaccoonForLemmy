package com.github.diegoberaldin.raccoonforlemmy.feature.settings.di

import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutDialogMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getSettingsViewModel(): SettingsMviModel = SettingsScreenModelHelper.settingsViewModel
actual fun getAboutDialogViewModel(): AboutDialogMviModel = SettingsScreenModelHelper.aboutViewModel

object SettingsScreenModelHelper : KoinComponent {
    val settingsViewModel: SettingsMviModel by inject()
    val aboutViewModel: AboutDialogMviModel by inject()
}
