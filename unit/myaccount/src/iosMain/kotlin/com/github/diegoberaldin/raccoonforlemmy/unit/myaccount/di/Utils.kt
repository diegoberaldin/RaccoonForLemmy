package com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.di

import com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.ProfileLoggedMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getProfileLoggedViewModel(): ProfileLoggedMviModel =
    UnitMyAccountDiHelper.loggedModel

object UnitMyAccountDiHelper : KoinComponent {
    val loggedModel: ProfileLoggedMviModel by inject()
}