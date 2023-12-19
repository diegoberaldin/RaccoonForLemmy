package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getUserDetailViewModel(user: UserModel, otherInstance: String): UserDetailMviModel =
    CommonUiViewModelHelper.getUserDetailModel(user, otherInstance)

object CommonUiViewModelHelper : KoinComponent {

    fun getUserDetailModel(user: UserModel, otherInstance: String): UserDetailMviModel {
        val model: UserDetailMviModel by inject(
            parameters = { parametersOf(user, otherInstance) },
        )
        return model
    }
}
