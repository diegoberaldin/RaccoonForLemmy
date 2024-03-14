package com.github.diegoberaldin.raccoonforlemmy.core.notifications.di

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import org.koin.java.KoinJavaComponent.inject

actual fun getNotificationCenter(): NotificationCenter {
    val res: NotificationCenter by inject(NotificationCenter::class.java)
    return res
}
