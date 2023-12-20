package com.github.diegoberaldin.raccoonforlemmy.unit.modlog.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.ModlogMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.ModlogViewModel
import org.koin.dsl.module

val modlogModule = module {
    factory<ModlogMviModel> { params ->
        ModlogViewModel(
            mvi = DefaultMviModel(ModlogMviModel.UiState()),
            communityId = params[0],
            themeRepository = get(),
            identityRepository = get(),
            modlogRepository = get(),
        )
    }
}