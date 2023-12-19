package com.github.diegoberaldin.raccoonforlemmy.core.notifications.di

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.ContentResetCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter

expect fun getNotificationCenter(): NotificationCenter

expect fun getContentResetCoordinator(): ContentResetCoordinator
