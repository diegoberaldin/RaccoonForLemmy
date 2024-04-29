package com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.di

import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.UserDetailViewModel
import org.koin.dsl.module

val userDetailModule = module {
    factory<UserDetailMviModel> { params ->
        UserDetailViewModel(
            userId = params[0],
            otherInstance = params[1],
            identityRepository = get(),
            apiConfigurationRepository = get(),
            userRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            hapticFeedback = get(),
            notificationCenter = get(),
            imagePreloadManager = get(),
            getSortTypesUseCase = get(),
            itemCache = get(),
            postPaginationManager = get(),
            commentPaginationManager = get(),
            postNavigationManager = get(),
        )
    }
}
