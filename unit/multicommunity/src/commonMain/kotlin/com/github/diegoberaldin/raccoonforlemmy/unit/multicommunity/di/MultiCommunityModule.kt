package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.di

import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.detail.MultiCommunityMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.detail.MultiCommunityViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.editor.MultiCommunityEditorMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.editor.MultiCommunityEditorViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.utils.DefaultMultiCommunityPaginator
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.utils.MultiCommunityPaginator
import org.koin.dsl.module

val multiCommunityModule = module {
    factory<MultiCommunityMviModel> { params ->
        MultiCommunityViewModel(
            communityId = params[0],
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
            getSortTypesUseCase = get(),
            multiCommunityRepository = get(),
        )
    }
    factory<MultiCommunityPaginator> {
        DefaultMultiCommunityPaginator(
            postRepository = get(),
        )
    }
    factory<MultiCommunityEditorMviModel> { params ->
        MultiCommunityEditorViewModel(
            communityId = params[0],
            identityRepository = get(),
            communityRepository = get(),
            accountRepository = get(),
            multiCommunityRepository = get(),
            notificationCenter = get(),
            settingsRepository = get(),
        )
    }
}