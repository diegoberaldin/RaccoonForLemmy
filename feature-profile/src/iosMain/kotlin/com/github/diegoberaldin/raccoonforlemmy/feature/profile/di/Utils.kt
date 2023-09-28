package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged.ProfileLoggedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheetViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts.ManageAccountsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.saved.ProfileSavedViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getProfileScreenModel() = ProfileScreenModelHelper.profileModel

actual fun getLoginBottomSheetViewModel() = ProfileScreenModelHelper.loginModel

actual fun getProfileLoggedViewModel(): ProfileLoggedViewModel =
    ProfileScreenModelHelper.loggedModel

actual fun getProfileSavedViewModel(
    user: UserModel,
): ProfileSavedViewModel =
    ProfileScreenModelHelper.getSavedModel(user = user)

actual fun getManageAccountsViewModel(): ManageAccountsViewModel =
    ProfileScreenModelHelper.manageAccountsViewModel

object ProfileScreenModelHelper : KoinComponent {
    val profileModel: ProfileMainViewModel by inject()
    val loginModel: LoginBottomSheetViewModel by inject()
    val loggedModel: ProfileLoggedViewModel by inject()
    val manageAccountsViewModel: ManageAccountsViewModel by inject()

    fun getSavedModel(user: UserModel): ProfileSavedViewModel {
        val res: ProfileSavedViewModel by inject(
            parameters = { parametersOf(user) },
        )
        return res
    }
}
