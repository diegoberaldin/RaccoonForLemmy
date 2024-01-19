package com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions.ConfigureSwipeActionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions.ConfigureSwipeActionsViewModel
import org.koin.dsl.module

val configureSwipeActionsModule = module {
    factory<ConfigureSwipeActionsMviModel> {
        ConfigureSwipeActionsViewModel(
            mvi = DefaultMviModel(ConfigureSwipeActionsMviModel.UiState()),
            settingsRepository = get(),
            accountRepository = get(),
            notificationCenter = get(),
        )
    }
}