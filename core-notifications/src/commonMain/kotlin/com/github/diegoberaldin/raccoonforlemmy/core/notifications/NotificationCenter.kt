package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterIsInstance

/**
 * Utility to publish and subscribe for broadcast notifications.
 */
@Stable
interface NotificationCenter {
    fun send(event: NotificationCenterEvent)

    val events: SharedFlow<NotificationCenterEvent>
}

inline fun <reified T> NotificationCenter.subscribe(): Flow<T> = events.filterIsInstance()
