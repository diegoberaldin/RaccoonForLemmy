package com.github.diegoberaldin.raccoonforlemmy.unit.saveditems.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.saveditems.SavedItemsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.saveditems.SavedItemsViewModel
import org.koin.dsl.module

val savedItemsModule = module {
    factory<SavedItemsMviModel> {
        SavedItemsViewModel(
            mvi = DefaultMviModel(SavedItemsMviModel.UiState()),
            identityRepository = get(),
            apiConfigurationRepository = get(),
            siteRepository = get(),
            userRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            hapticFeedback = get(),
            notificationCenter = get(),
            getSortTypesUseCase = get(),
        )
    }
}