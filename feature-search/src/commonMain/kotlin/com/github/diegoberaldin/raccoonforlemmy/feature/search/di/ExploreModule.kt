package com.github.diegoberaldin.raccoonforlemmy.feature.search.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions.ManageSubscriptionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions.ManageSubscriptionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail.MultiCommunityMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail.MultiCommunityViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.editor.MultiCommunityEditorMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.editor.MultiCommunityEditorViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.utils.DefaultMultiCommunityPaginator
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.utils.MultiCommunityPaginator
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
        )
    }
    factory<ManageSubscriptionsMviModel> {
        ManageSubscriptionsViewModel(
            mvi = DefaultMviModel(ManageSubscriptionsMviModel.UiState()),
            identityRepository = get(),
            communityRepository = get(),
            accountRepository = get(),
            multiCommunityRepository = get(),
            hapticFeedback = get(),
            notificationCenter = get(),
            settingsRepository = get(),
        )
    }
    factory<MultiCommunityMviModel> { params ->
        MultiCommunityViewModel(
            mvi = DefaultMviModel(MultiCommunityMviModel.UiState()),
            community = params[0],
            postRepository = get(),
            identityRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            shareHelper = get(),
            settingsRepository = get(),
            notificationCenter = get(),
            hapticFeedback = get(),
            paginator = get(),
            imagePreloadManager = get(),
        )
    }
    factory<MultiCommunityPaginator> {
        DefaultMultiCommunityPaginator(
            postRepository = get(),
        )
    }
    factory<MultiCommunityEditorMviModel> { params ->
        MultiCommunityEditorViewModel(
            mvi = DefaultMviModel(MultiCommunityEditorMviModel.UiState()),
            editedCommunity = params[0],
            identityRepository = get(),
            communityRepository = get(),
            accountRepository = get(),
            multiCommunityRepository = get(),
            notificationCenter = get(),
            settingsRepository = get(),
        )
    }
}
