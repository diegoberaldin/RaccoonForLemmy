package com.github.diegoberaldin.raccoonforlemmy.unit.messages.di

import com.github.diegoberaldin.raccoonforlemmy.unit.messages.InboxMessagesMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.messages.InboxMessagesViewModel
import org.koin.dsl.module

val inboxMessagesModule = module {
    factory<InboxMessagesMviModel> {
        InboxMessagesViewModel(
            identityRepository = get(),
            siteRepository = get(),
            messageRepository = get(),
            coordinator = get(),
            notificationCenter = get(),
            settingsRepository = get(),
        )
    }
}