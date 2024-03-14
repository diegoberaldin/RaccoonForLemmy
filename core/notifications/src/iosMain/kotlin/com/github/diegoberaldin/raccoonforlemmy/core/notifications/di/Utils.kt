package com.github.diegoberaldin.raccoonforlemmy.core.notifications.di

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getNotificationCenter(): NotificationCenter = NotificationDiHelper.notificationCenter

internal object NotificationDiHelper : KoinComponent {
    val notificationCenter: NotificationCenter by inject()
}