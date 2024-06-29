package com.github.diegoberaldin.raccoonforlemmy.unit.configurecontentview.di

import com.github.diegoberaldin.raccoonforlemmy.unit.configurecontentview.ConfigureContentViewMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.configurecontentview.ConfigureContentViewViewModel
import org.koin.dsl.module

val configureContentViewModule =
    module {
        factory<ConfigureContentViewMviModel> {
            ConfigureContentViewViewModel(
                themeRepository = get(),
                settingsRepository = get(),
                accountRepository = get(),
                notificationCenter = get(),
                identityRepository = get(),
                siteRepository = get(),
                lemmyValueCache = get(),
            )
        }
    }
