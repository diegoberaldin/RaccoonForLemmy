package com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.UserDetailMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject


actual fun getUserDetailViewModel(user: UserModel, otherInstance: String): UserDetailMviModel {
    val res: UserDetailMviModel by inject(
        clazz = UserDetailMviModel::class.java,
        parameters = { parametersOf(user, otherInstance) },
    )
    return res
}
