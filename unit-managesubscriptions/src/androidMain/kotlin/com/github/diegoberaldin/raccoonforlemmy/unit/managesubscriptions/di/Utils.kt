package com.github.diegoberaldin.raccoonforlemmy.unit.managesubscriptions.di

import com.github.diegoberaldin.raccoonforlemmy.unit.managesubscriptions.ManageSubscriptionsMviModel
import org.koin.java.KoinJavaComponent

actual fun getManageSubscriptionsViewModel(): ManageSubscriptionsMviModel {
    val res: ManageSubscriptionsMviModel by KoinJavaComponent.inject(ManageSubscriptionsMviModel::class.java)
    return res
}
