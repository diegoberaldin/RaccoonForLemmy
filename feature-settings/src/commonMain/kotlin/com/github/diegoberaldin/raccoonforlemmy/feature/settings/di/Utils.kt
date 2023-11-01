package com.github.diegoberaldin.raccoonforlemmy.feature.settings.di

import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutDialogMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsMviModel

expect fun getSettingsViewModel(): SettingsMviModel

expect fun getAboutDialogViewModel(): AboutDialogMviModel
