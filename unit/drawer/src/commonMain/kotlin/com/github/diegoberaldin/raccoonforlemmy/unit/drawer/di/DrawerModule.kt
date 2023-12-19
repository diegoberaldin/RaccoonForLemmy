package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.ModalDrawerMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.ModalDrawerViewModel
import org.koin.dsl.module

val drawerModule = module {
    factory<ModalDrawerMviModel> {
        ModalDrawerViewModel(
            mvi = DefaultMviModel(ModalDrawerMviModel.UiState()),
            apiConfigurationRepository = get(),
            siteRepository = get(),
            identityRepository = get(),
            accountRepository = get(),
            communityRepository = get(),
            multiCommunityRepository = get(),
            settingsRepository = get(),
        )
    }
}