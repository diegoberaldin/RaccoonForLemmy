package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

object DefaultNotificationCenter : NotificationCenter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val events = MutableSharedFlow<NotificationCenterEvent>()

    override fun send(event: NotificationCenterEvent) {
        scope.launch(Dispatchers.Main) {
            events.emit(event)
        }
    }

    override fun <T : NotificationCenterEvent> subscribe(clazz: KClass<T>): Flow<T> {
        return events.mapNotNull { clazz.safeCast(it) }
    }
}
