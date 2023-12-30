package com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.CommunityInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.CommunityInfoViewModel
import org.koin.dsl.module

val communityInfoModule = module {
    factory<CommunityInfoMviModel> { params ->
        CommunityInfoViewModel(
            mvi = DefaultMviModel(CommunityInfoMviModel.UiState()),
            communityId = params[0],
            communityRepository = get(),
            settingsRepository = get(),
            itemCache = get(),
        )
    }
}