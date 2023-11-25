package com.github.diegoberaldin.raccoonforlemmy.core.notifications.di

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.ContentResetCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import org.koin.java.KoinJavaComponent.inject

actual fun getNotificationCenter(): NotificationCenter {
    val res: NotificationCenter by inject(NotificationCenter::class.java)
    return res
}

actual fun getContentResetCoordinator(): ContentResetCoordinator {
    val res: ContentResetCoordinator by inject(ContentResetCoordinator::class.java)
    return res
}
