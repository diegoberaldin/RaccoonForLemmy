package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getInboxViewModel(): InboxMviModel = InboxScreenModelHelper.model

object InboxScreenModelHelper : KoinComponent {
    val model: InboxMviModel by inject()
}
