package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * Utility to publish and subscribe for broadcast notifications.
 */
@Stable
interface NotificationCenter {
    fun send(event: NotificationCenterEvent)

    fun <T : NotificationCenterEvent> subscribe(clazz: KClass<T>): Flow<T>

    fun resetCache()
}
