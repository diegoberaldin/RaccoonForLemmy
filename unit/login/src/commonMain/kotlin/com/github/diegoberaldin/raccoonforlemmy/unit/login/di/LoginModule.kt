package com.github.diegoberaldin.raccoonforlemmy.unit.login.di

import com.github.diegoberaldin.raccoonforlemmy.unit.login.LoginMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.login.LoginViewModel
import org.koin.dsl.module

val loginModule =
    module {
        factory<LoginMviModel> {
            LoginViewModel(
                login = get(),
                accountRepository = get(),
                identityRepository = get(),
                siteRepository = get(),
                communityRepository = get(),
                apiConfigurationRepository = get(),
                notificationCenter = get(),
            )
        }
    }
