package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.di

import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.ModalDrawerMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.ModalDrawerViewModel
import org.koin.dsl.module

val drawerModule = module {
    factory<ModalDrawerMviModel> {
        ModalDrawerViewModel(
            apiConfigurationRepository = get(),
            siteRepository = get(),
            identityRepository = get(),
            accountRepository = get(),
            communityRepository = get(),
            multiCommunityRepository = get(),
            settingsRepository = get(),
            favoriteCommunityRepository = get(),
        )
    }
}