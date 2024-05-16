package com.github.diegoberaldin.raccoonforlemmy.unit.createpost.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostViewModel
import org.koin.dsl.module

val createPostModule =
    module {
        factory<CreatePostMviModel> { params ->
            CreatePostViewModel(
                editedPostId = params[0],
                crossPostId = params[1],
                draftId = params[2],
                identityRepository = get(),
                postRepository = get(),
                siteRepository = get(),
                themeRepository = get(),
                settingsRepository = get(),
                itemCache = get(),
                communityRepository = get(),
                accountRepository = get(),
                draftRepository = get(),
                notificationCenter = get(),
            )
        }
    }
