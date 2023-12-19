package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.di

import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.ModalDrawerMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getModalDrawerViewModel(): ModalDrawerMviModel =
    UnitDrawerDiHelper.modalDrawerViewModel

object UnitDrawerDiHelper : KoinComponent {
    val modalDrawerViewModel: ModalDrawerMviModel by inject()
}