package com.github.diegoberaldin.raccoonforlemmy.feature.settings.di

import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutDialogMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsMviModel
import org.koin.java.KoinJavaComponent.inject

actual fun getSettingsViewModel(): SettingsMviModel {
    val res: SettingsMviModel by inject(SettingsMviModel::class.java)
    return res
}

actual fun getAboutDialogViewModel(): AboutDialogMviModel {
    val res: AboutDialogMviModel by inject(AboutDialogMviModel::class.java)
    return res
}
