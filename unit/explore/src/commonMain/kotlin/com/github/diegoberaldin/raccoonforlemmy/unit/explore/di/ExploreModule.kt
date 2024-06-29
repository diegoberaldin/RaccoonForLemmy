package com.github.diegoberaldin.raccoonforlemmy.unit.explore.di

import com.github.diegoberaldin.raccoonforlemmy.unit.explore.ExploreMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.explore.ExploreViewModel
import org.koin.dsl.module

val exploreModule =
    module {
        factory<ExploreMviModel> { params ->
            ExploreViewModel(
                otherInstance = params[0],
                apiConfigRepository = get(),
                identityRepository = get(),
                communityRepository = get(),
                userRepository = get(),
                postRepository = get(),
                commentRepository = get(),
                themeRepository = get(),
                settingsRepository = get(),
                notificationCenter = get(),
                hapticFeedback = get(),
                getSortTypesUseCase = get(),
                lemmyValueCache = get(),
            )
        }
    }
