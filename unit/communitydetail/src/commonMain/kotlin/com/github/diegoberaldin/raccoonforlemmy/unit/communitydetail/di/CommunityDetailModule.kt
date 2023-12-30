package com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.CommunityDetailViewModel
import org.koin.dsl.module

val communityDetailModule = module {
    factory<CommunityDetailMviModel> { params ->
        CommunityDetailViewModel(
            mvi = DefaultMviModel(CommunityDetailMviModel.UiState()),
            communityId = params[0],
            otherInstance = params[1],
            identityRepository = get(),
            apiConfigurationRepository = get(),
            communityRepository = get(),
            postRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            hapticFeedback = get(),
            zombieModeHelper = get(),
            imagePreloadManager = get(),
            notificationCenter = get(),
            getSortTypesUseCase = get(),
            accountRepository = get(),
            favoriteCommunityRepository = get(),
            itemCache = get(),
        )
    }
}