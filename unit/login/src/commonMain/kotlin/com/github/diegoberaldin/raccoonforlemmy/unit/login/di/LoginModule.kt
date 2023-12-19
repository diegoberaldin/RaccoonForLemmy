package com.github.diegoberaldin.raccoonforlemmy.unit.login.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.login.LoginBottomSheetMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.login.LoginBottomSheetViewModel
import org.koin.dsl.module

val loginModule = module {
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
}