package com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.di

import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsViewModel
import org.koin.dsl.module

val manageAccountsModule = module {
    factory<ManageAccountsMviModel> {
        ManageAccountsViewModel(
            accountRepository = get(),
            settingsRepository = get(),
            switchAccount = get(),
            logout = get(),
            deleteAccount = get(),
            notificationCenter = get(),
        )
    }
}