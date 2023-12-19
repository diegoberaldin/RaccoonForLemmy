package com.github.diegoberaldin.raccoonforlemmy.unit.login.di

import com.github.diegoberaldin.raccoonforlemmy.unit.login.LoginBottomSheetMviModel
import org.koin.java.KoinJavaComponent

actual fun getLoginBottomSheetViewModel(): LoginBottomSheetMviModel {
    val res: LoginBottomSheetMviModel by KoinJavaComponent.inject(LoginBottomSheetMviModel::class.java)
    return res
}
