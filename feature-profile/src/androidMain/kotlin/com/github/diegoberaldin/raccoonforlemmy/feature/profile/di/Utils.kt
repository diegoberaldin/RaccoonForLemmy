package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged.ProfileLoggedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheetViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts.ManageAccountsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.saved.ProfileSavedViewModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject

actual fun getProfileScreenModel(): ProfileMainViewModel {
    val res: ProfileMainViewModel by inject(ProfileMainViewModel::class.java)
    return res
}

actual fun getLoginBottomSheetViewModel(): LoginBottomSheetViewModel {
    val res: LoginBottomSheetViewModel by inject(LoginBottomSheetViewModel::class.java)
    return res
}

actual fun getProfileLoggedViewModel(): ProfileLoggedViewModel {
    val res: ProfileLoggedViewModel by inject(ProfileLoggedViewModel::class.java)
    return res
}

actual fun getProfileSavedViewModel(user: UserModel): ProfileSavedViewModel {
    val res: ProfileSavedViewModel by inject(
        clazz = ProfileSavedViewModel::class.java,
        parameters = { parametersOf(user) },
    )
    return res
}

actual fun getManageAccountsViewModel(): ManageAccountsViewModel {
    val res: ManageAccountsViewModel by inject(ManageAccountsViewModel::class.java)
    return res
}
