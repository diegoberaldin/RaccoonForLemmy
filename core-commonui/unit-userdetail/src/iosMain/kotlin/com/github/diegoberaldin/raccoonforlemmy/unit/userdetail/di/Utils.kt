package com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.UserDetailMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getUserDetailViewModel(user: UserModel, otherInstance: String): UserDetailMviModel =
    UnitUserDetailDiHelper.getUserDetailModel(user, otherInstance)

object UnitUserDetailDiHelper : KoinComponent {

    fun getUserDetailModel(user: UserModel, otherInstance: String): UserDetailMviModel {
        val model: UserDetailMviModel by inject(
            parameters = { parametersOf(user, otherInstance) },
        )
        return model
    }
}
