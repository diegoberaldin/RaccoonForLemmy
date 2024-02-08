package com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.di

import com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.ProfileLoggedMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.ProfileLoggedViewModel
import org.koin.dsl.module

val myAccountModule = module {
    factory<ProfileLoggedMviModel> {
        ProfileLoggedViewModel(
            identityRepository = get(),
            apiConfigurationRepository = get(),
            siteRepository = get(),
            userRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            notificationCenter = get(),
            hapticFeedback = get(),
        )
    }
}