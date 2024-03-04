package com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.di

import com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.comments.ModdedCommentsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.comments.ModdedCommentsViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.posts.ModdedPostsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.posts.ModdedPostsViewModel
import org.koin.dsl.module

val moddedContentsModule = module {
    factory<ModdedCommentsMviModel> {
        ModdedCommentsViewModel(
            themeRepository = get(),
            settingsRepository = get(),
            identityRepository = get(),
            commentRepository = get(),
            hapticFeedback = get(),
        )
    }
    factory<ModdedPostsMviModel> {
        ModdedPostsViewModel(
            themeRepository = get(),
            settingsRepository = get(),
            identityRepository = get(),
            postRepository = get(),
            hapticFeedback = get(),
            imagePreloadManager = get(),
            notificationCenter = get(),
        )
    }
}