package com.github.diegoberaldin.raccoonforlemmy.domain.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.notification.DefaultInboxNotificationChecker
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.notification.InboxNotificationChecker
import org.koin.dsl.module

actual val inboxNativeModule = module {
    single<InboxNotificationChecker> {
        DefaultInboxNotificationChecker(
            context = get(),
        )
    }
}
