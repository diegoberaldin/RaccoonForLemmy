package com.github.diegoberaldin.raccoonforlemmy.di

import com.github.diegoberaldin.raccoonforlemmy.MainScreenMviModel
import com.github.diegoberaldin.raccoonforlemmy.MainViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.DetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.impl.DefaultDetailOpener
import org.koin.dsl.module

internal val internalSharedModule = module {
    factory<MainScreenMviModel> {
        MainViewModel(
            mvi = DefaultMviModel(MainScreenMviModel.UiState()),
            inboxCoordinator = get(),
        )
    }
    single<DetailOpener> {
        DefaultDetailOpener(
            navigationCoordinator = get(),
            itemCache = get(),
        )
    }
}