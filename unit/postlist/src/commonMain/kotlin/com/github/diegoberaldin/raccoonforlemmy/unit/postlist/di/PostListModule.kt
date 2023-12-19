package com.github.diegoberaldin.raccoonforlemmy.unit.postlist.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.postlist.PostListMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.postlist.PostListViewModel
import org.koin.dsl.module

val postListModule = module {
    factory<PostListMviModel> {
        PostListViewModel(
            mvi = DefaultMviModel(PostListMviModel.UiState()),
            postRepository = get(),
            apiConfigurationRepository = get(),
            identityRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            notificationCenter = get(),
            hapticFeedback = get(),
            zombieModeHelper = get(),
            imagePreloadManager = get(),
            contentResetCoordinator = get(),
            getSortTypesUseCase = get(),
        )
    }
}