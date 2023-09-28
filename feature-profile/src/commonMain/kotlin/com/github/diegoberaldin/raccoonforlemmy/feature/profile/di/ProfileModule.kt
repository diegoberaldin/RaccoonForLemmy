package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged.ProfileLoggedMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged.ProfileLoggedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheetMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheetViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts.ManageAccountsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts.ManageAccountsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.saved.ProfileSavedMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.saved.ProfileSavedViewModel
import org.koin.dsl.module

val profileTabModule = module {
    factory {
        ProfileMainViewModel(
            mvi = DefaultMviModel(ProfileMainMviModel.UiState()),
            identityRepository = get(),
            keyStore = get(),
            logout = get(),
        )
    }
    factory {
        LoginBottomSheetViewModel(
            mvi = DefaultMviModel(LoginBottomSheetMviModel.UiState()),
            login = get(),
            accountRepository = get(),
            identityRepository = get(),
            siteRepository = get(),
        )
    }
    factory {
        ProfileLoggedViewModel(
            mvi = DefaultMviModel(ProfileLoggedMviModel.UiState()),
            identityRepository = get(),
            siteRepository = get(),
            userRepository = get(),
            postsRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            shareHelper = get(),
            notificationCenter = get(),
        )
    }
    factory { params ->
        ProfileSavedViewModel(
            mvi = DefaultMviModel(ProfileSavedMviModel.UiState()),
            user = params[0],
            identityRepository = get(),
            userRepository = get(),
            postsRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            hapticFeedback = get(),
            notificationCenter = get(),
        )
    }
    factory {
        ManageAccountsViewModel(
            mvi = DefaultMviModel(ManageAccountsMviModel.UiState()),
            accountRepository = get(),
            switchAccount = get(),
        )
    }
}
