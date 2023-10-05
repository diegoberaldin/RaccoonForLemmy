package com.github.diegoberaldin.raccoonforlemmy.feature.search.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions.ManageSubscriptionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions.ManageSubscriptionsViewModel
import org.koin.dsl.module

val searchTabModule = module {
    factory {
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
        )
    }
    factory {
        ManageSubscriptionsViewModel(
            mvi = DefaultMviModel(ManageSubscriptionsMviModel.UiState()),
            identityRepository = get(),
            communityRepository = get(),
            accountRepository = get(),
            multiCommunityRepository = get(),
            hapticFeedback = get(),
            notificationCenter = get(),
        )
    }
}
