package com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.UserInfoMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getUserInfoViewModel(user: UserModel): UserInfoMviModel =
    UnitUserInfoDiHelper.getUserInfoViewModel(user)

object UnitUserInfoDiHelper : KoinComponent {
    fun getUserInfoViewModel(user: UserModel): UserInfoMviModel {
        val res: UserInfoMviModel by inject(parameters = { parametersOf(user) })
        return res
    }
}