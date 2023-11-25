package com.github.diegoberaldin.raccoonforlemmy.core.notifications.di

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.ContentResetCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getNotificationCenter(): NotificationCenter = NotificationDiHelper.notificationCenter

actual fun getContentResetCoordinator(): ContentResetCoordinator =
    NotificationDiHelper.contentResetCoordinator

internal object NotificationDiHelper : KoinComponent {
    val notificationCenter: NotificationCenter by inject()
    val contentResetCoordinator: ContentResetCoordinator by inject()
}