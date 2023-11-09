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
import org.koin.dsl.module

val profileTabModule = module {
    factory<ProfileMainMviModel> {
        ProfileMainViewModel(
            mvi = DefaultMviModel(ProfileMainMviModel.UiState()),
            identityRepository = get(),
            logout = get(),
        )
    }
    factory<LoginBottomSheetMviModel> {
        LoginBottomSheetViewModel(
            mvi = DefaultMviModel(LoginBottomSheetMviModel.UiState()),
            login = get(),
            accountRepository = get(),
            identityRepository = get(),
            siteRepository = get(),
            communityRepository = get(),
            apiConfigurationRepository = get(),
        )
    }
    factory<ProfileLoggedMviModel> {
        ProfileLoggedViewModel(
            mvi = DefaultMviModel(ProfileLoggedMviModel.UiState()),
            identityRepository = get(),
            siteRepository = get(),
            userRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            notificationCenter = get(),
            hapticFeedback = get(),
        )
    }
    factory<ManageAccountsMviModel> {
        ManageAccountsViewModel(
            mvi = DefaultMviModel(ManageAccountsMviModel.UiState()),
            accountRepository = get(),
            switchAccount = get(),
            settingsRepository = get(),
        )
    }
}
