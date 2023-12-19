package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.login.di.loginModule
import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.di.manageAccountsModule
import org.koin.dsl.module

val profileTabModule = module {
    includes(
        loginModule,
        manageAccountsModule,
    )
    factory<ProfileMainMviModel> {
        ProfileMainViewModel(
            mvi = DefaultMviModel(ProfileMainMviModel.UiState()),
            identityRepository = get(),
            logout = get(),
        )
    }
}
