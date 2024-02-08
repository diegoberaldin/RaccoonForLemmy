package com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.di

import com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.AccountSettingsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.AccountSettingsViewModel
import org.koin.dsl.module

val accountSettingsModule = module {
    factory<AccountSettingsMviModel> {
        AccountSettingsViewModel(
            siteRepository = get(),
            identityRepository = get(),
            postRepository = get(),
            getSortTypesUseCase = get(),
            notificationCenter = get(),
        )
    }
}