package com.github.diegoberaldin.raccoonforlemmy.unit.createpost.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostViewModel
import org.koin.dsl.module

val createPostModule = module {
    factory<CreatePostMviModel> { params ->
        CreatePostViewModel(
            mvi = DefaultMviModel(CreatePostMviModel.UiState()),
            editedPostId = params[0],
            identityRepository = get(),
            postRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
        )
    }
}