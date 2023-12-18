package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.di

import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.ModalDrawerMviModel
import org.koin.java.KoinJavaComponent

actual fun getModalDrawerViewModel(): ModalDrawerMviModel {
    val res: ModalDrawerMviModel by KoinJavaComponent.inject(ModalDrawerMviModel::class.java)
    return res
}
