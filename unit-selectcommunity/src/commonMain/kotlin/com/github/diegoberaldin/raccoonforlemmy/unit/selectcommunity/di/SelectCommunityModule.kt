package com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.SelectCommunityMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.SelectCommunityViewModel
import org.koin.dsl.module

val selectCommunityModule = module {
    factory<SelectCommunityMviModel> {
        SelectCommunityViewModel(
            mvi = DefaultMviModel(SelectCommunityMviModel.UiState()),
            identityRepository = get(),
            communityRepository = get(),
            settingsRepository = get(),
            notificationCenter = get(),
        )
    }
}