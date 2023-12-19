package com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsViewModel
import org.koin.dsl.module

val manageAccountsModule = module {
    factory<ManageAccountsMviModel> {
        ManageAccountsViewModel(
            mvi = DefaultMviModel(ManageAccountsMviModel.UiState()),
            accountRepository = get(),
            settingsRepository = get(),
            switchAccount = get(),
            logout = get(),
            deleteAccount = get(),
        )
    }
}