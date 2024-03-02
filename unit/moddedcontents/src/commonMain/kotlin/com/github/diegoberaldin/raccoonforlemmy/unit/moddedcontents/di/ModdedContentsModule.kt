package com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.di

import com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.comments.ModdedCommentsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.comments.ModdedCommentsViewModel
import org.koin.dsl.module

val moddedContentsModule = module {
    factory<ModdedCommentsMviModel> { params ->
        ModdedCommentsViewModel(
            themeRepository = get(),
            settingsRepository = get(),
            identityRepository = get(),
            commentRepository = get(),
            hapticFeedback = get(),
        )
    }
}