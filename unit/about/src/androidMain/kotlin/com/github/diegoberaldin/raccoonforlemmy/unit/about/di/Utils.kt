package com.github.diegoberaldin.raccoonforlemmy.unit.about.di

import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutDialogMviModel
import org.koin.java.KoinJavaComponent

actual fun getAboutDialogViewModel(): AboutDialogMviModel {
    val res: AboutDialogMviModel by KoinJavaComponent.inject(AboutDialogMviModel::class.java)
    return res
}
