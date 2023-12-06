package com.github.diegoberaldin.raccoonforlemmy.di

import com.github.diegoberaldin.raccoonforlemmy.MainScreenMviModel
import com.github.diegoberaldin.raccoonforlemmy.MainViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import org.koin.dsl.module

internal val internalSharedModule = module {
    factory<MainScreenMviModel> {
        MainViewModel(
            mvi = DefaultMviModel(MainScreenMviModel.UiState()),
            inboxCoordinator = get(),
        )
    }
}