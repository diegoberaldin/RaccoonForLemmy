package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.di

import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.PostDetailViewModel
import org.koin.dsl.module

val postDetailModule = module {
    factory<PostDetailMviModel> { params ->
        PostDetailViewModel(
            postId = params[0],
            otherInstance = params[1],
            highlightCommentId = params[2],
            isModerator = params[3],
            identityRepository = get(),
            apiConfigurationRepository = get(),
            siteRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            communityRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            notificationCenter = get(),
            hapticFeedback = get(),
            getSortTypesUseCase = get(),
            itemCache = get(),
            commentPaginationManager = get(),
            postNavigationManager = get(),
        )
    }
}
