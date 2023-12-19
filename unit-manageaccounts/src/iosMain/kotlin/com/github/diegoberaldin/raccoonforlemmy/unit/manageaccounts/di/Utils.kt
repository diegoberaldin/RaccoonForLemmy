package com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.di

import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getManageAccountsViewModel(): ManageAccountsMviModel =
    UnitManageAccountsDiHelper.manageAccountsViewModel

object UnitManageAccountsDiHelper : KoinComponent {
    val manageAccountsViewModel: ManageAccountsMviModel by inject()
}