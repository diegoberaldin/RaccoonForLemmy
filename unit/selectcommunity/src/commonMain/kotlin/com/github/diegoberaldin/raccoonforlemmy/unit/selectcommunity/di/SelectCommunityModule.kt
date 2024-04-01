package com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.di

import com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.SelectCommunityMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.SelectCommunityViewModel
import org.koin.dsl.module

val selectCommunityModule = module {
    factory<SelectCommunityMviModel> {
        SelectCommunityViewModel(
            identityRepository = get(),
            communityRepository = get(),
            settingsRepository = get(),
        )
    }
}
