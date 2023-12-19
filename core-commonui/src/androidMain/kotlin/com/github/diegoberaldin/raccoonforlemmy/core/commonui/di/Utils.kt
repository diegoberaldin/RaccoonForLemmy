package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject


actual fun getUserDetailViewModel(user: UserModel, otherInstance: String): UserDetailMviModel {
    val res: UserDetailMviModel by inject(
        clazz = UserDetailMviModel::class.java,
        parameters = { parametersOf(user, otherInstance) },
    )
    return res
}
