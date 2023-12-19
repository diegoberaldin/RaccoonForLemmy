package com.github.diegoberaldin.raccoonforlemmy.unit.login.di

import com.github.diegoberaldin.raccoonforlemmy.unit.login.LoginBottomSheetMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getLoginBottomSheetViewModel(): LoginBottomSheetMviModel =
    UnitLoginDiHelper.loginModel

object UnitLoginDiHelper : KoinComponent {
    val loginModel: LoginBottomSheetMviModel by inject()
}