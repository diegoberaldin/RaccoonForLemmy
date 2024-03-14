package com.github.diegoberaldin.raccoonforlemmy.core.notifications.di

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.DefaultNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import org.koin.dsl.module

val coreNotificationModule = module {
    single<NotificationCenter> {
        DefaultNotificationCenter
    }
}
