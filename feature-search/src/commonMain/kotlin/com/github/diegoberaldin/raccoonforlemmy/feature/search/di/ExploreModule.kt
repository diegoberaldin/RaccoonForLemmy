package com.github.diegoberaldin.raccoonforlemmy.feature.search.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreViewModel
import org.koin.dsl.module

val exploreTabModule = module {
    factory<ExploreMviModel> {
        ExploreViewModel(
            mvi = DefaultMviModel(ExploreMviModel.UiState()),
            apiConfigRepository = get(),
            identityRepository = get(),
            communityRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            notificationCenter = get(),
            hapticFeedback = get(),
            contentResetCoordinator = get(),
            getSortTypesUseCase = get(),
        )
    }
}
