package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged.ProfileLoggedMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts.ManageAccountsMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getProfileScreenModel(): ProfileMainMviModel = ProfileScreenModelHelper.profileModel

actual fun getProfileLoggedViewModel(): ProfileLoggedMviModel =
    ProfileScreenModelHelper.loggedModel

actual fun getManageAccountsViewModel(): ManageAccountsMviModel =
    ProfileScreenModelHelper.manageAccountsViewModel

object ProfileScreenModelHelper : KoinComponent {
    val profileModel: ProfileMainMviModel by inject()
    val loggedModel: ProfileLoggedMviModel by inject()
    val manageAccountsViewModel: ManageAccountsMviModel by inject()
}
