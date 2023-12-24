package com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.UserInfoMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject

actual fun getUserInfoViewModel(user: UserModel): UserInfoMviModel {
    val res: UserInfoMviModel by inject(
        UserInfoMviModel::class.java,
        parameters = { parametersOf(user) })
    return res
}