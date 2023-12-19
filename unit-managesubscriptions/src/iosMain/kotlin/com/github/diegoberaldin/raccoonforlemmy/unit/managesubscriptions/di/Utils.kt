package com.github.diegoberaldin.raccoonforlemmy.unit.managesubscriptions.di

import com.github.diegoberaldin.raccoonforlemmy.unit.managesubscriptions.ManageSubscriptionsMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getManageSubscriptionsViewModel(): ManageSubscriptionsMviModel =
    UnitManageSubscriptionsDiHelper.manageSuscriptionsViewModel

object UnitManageSubscriptionsDiHelper : KoinComponent {
    val manageSuscriptionsViewModel: ManageSubscriptionsMviModel by inject()
}