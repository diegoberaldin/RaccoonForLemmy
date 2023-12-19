package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged.ProfileLoggedMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged.ProfileLoggedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts.ManageAccountsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts.ManageAccountsViewModel
import org.koin.dsl.module

val profileTabModule = module {
    includes(loginModule)
    factory<ProfileMainMviModel> {
        ProfileMainViewModel(
            mvi = DefaultMviModel(ProfileMainMviModel.UiState()),
            identityRepository = get(),
            logout = get(),
        )
    }
    factory<ProfileLoggedMviModel> {
        ProfileLoggedViewModel(
            mvi = DefaultMviModel(ProfileLoggedMviModel.UiState()),
            identityRepository = get(),
            apiConfigurationRepository = get(),
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
            settingsRepository = get(),
            switchAccount = get(),
            logout = get(),
            deleteAccount = get(),
        )
    }
}
