package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.notlogged.ProfileNotLoggedMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.notlogged.ProfileNotLoggedViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.login.di.loginModule
import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.di.manageAccountsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.di.myAccountModule
import org.koin.dsl.module

val profileTabModule =
    module {
        includes(
            loginModule,
            manageAccountsModule,
            myAccountModule,
        )
        factory<ProfileMainMviModel> {
            ProfileMainViewModel(
                identityRepository = get(),
                logout = get(),
            )
        }
        factory<ProfileNotLoggedMviModel> {
            ProfileNotLoggedViewModel(
                identityRepository = get(),
            )
        }
    }
