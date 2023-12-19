package com.github.diegoberaldin.raccoonforlemmy.unit.about.di

import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutDialogMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getAboutDialogViewModel(): AboutDialogMviModel = UnitAboutDiHelper.aboutViewModel

object UnitAboutDiHelper : KoinComponent {
    val aboutViewModel: AboutDialogMviModel by inject()
}