package com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.di

import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsMviModel
import org.koin.java.KoinJavaComponent

actual fun getManageAccountsViewModel(): ManageAccountsMviModel {
    val res: ManageAccountsMviModel by KoinJavaComponent.inject(ManageAccountsMviModel::class.java)
    return res
}
