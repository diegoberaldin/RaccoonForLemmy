package com.github.diegoberaldin.raccoonforlemmy.di

import com.github.diegoberaldin.raccoonforlemmy.MainMviModel
import com.github.diegoberaldin.raccoonforlemmy.MainViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.DetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.impl.DefaultDetailOpener
import org.koin.dsl.module

internal val internalSharedModule =
    module {
        factory<MainMviModel> {
            MainViewModel(
                inboxCoordinator = get(),
                identityRepository = get(),
                settingRepository = get(),
                userRepository = get(),
                notificationChecker = get(),
                lemmyValueCache = get(),
            )
        }
        single<DetailOpener> {
            DefaultDetailOpener(
                navigationCoordinator = get(),
                itemCache = get(),
                identityRepository = get(),
                communityRepository = get(),
            )
        }
    }
