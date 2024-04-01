package com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.di

import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.CommunityInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.CommunityInfoViewModel
import org.koin.dsl.module

val communityInfoModule = module {
    factory<CommunityInfoMviModel> { params ->
        CommunityInfoViewModel(
            communityId = params[0],
            communityRepository = get(),
            settingsRepository = get(),
            itemCache = get(),
        )
    }
}
