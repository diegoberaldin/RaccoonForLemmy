package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.ProfileContentMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.ProfileContentViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.ProfileLoggedMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.ProfileLoggedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.saved.ProfileSavedMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.saved.ProfileSavedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheetMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheetViewModel
import org.koin.dsl.module

val profileTabModule = module {
    factory {
        ProfileContentViewModel(
            mvi = DefaultMviModel(ProfileContentMviModel.UiState()),
            identityRepository = get(),
            keyStore = get(),
            notificationCenter = get(),
        )
    }
    factory {
        LoginBottomSheetViewModel(
            mvi = DefaultMviModel(LoginBottomSheetMviModel.UiState()),
            loginUseCase = get(),
        )
    }
    factory {
        ProfileLoggedViewModel(
            mvi = DefaultMviModel(ProfileLoggedMviModel.UiState()),
            identityRepository = get(),
            siteRepository = get(),
            userRepository = get(),
            postsRepository = get(),
            commentRepository = get(),
            shareHelper = get(),
            notificationCenter = get(),
        )
    }
    factory { params ->
        ProfileSavedViewModel(
            mvi = DefaultMviModel(ProfileSavedMviModel.UiState()),
            user = params[0],
            identityRepository = get(),
            userRepository = get(),
            postsRepository = get(),
            commentRepository = get(),
            hapticFeedback = get(),
            notificationCenter = get(),
        )
    }
}
