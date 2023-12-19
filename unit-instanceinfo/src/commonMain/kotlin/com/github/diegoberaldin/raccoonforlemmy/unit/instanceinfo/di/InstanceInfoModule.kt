package com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.InstanceInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.InstanceInfoViewModel
import org.koin.dsl.module

val instanceInfoModule = module {
    factory<InstanceInfoMviModel> {
        InstanceInfoViewModel(
            mvi = DefaultMviModel(InstanceInfoMviModel.UiState()),
            url = it[0],
            siteRepository = get(),
            communityRepository = get(),
            identityRepository = get(),
            settingsRepository = get(),
            notificationCenter = get(),
            getSortTypesUseCase = get(),
        )
    }
}