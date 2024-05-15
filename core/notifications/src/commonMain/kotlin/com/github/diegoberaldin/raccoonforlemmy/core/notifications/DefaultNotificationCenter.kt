package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

private const val REPLAY_EVENT_COUNT = 5

object DefaultNotificationCenter : NotificationCenter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val events = MutableSharedFlow<NotificationCenterEvent>()
    private val replayedEvents = MutableSharedFlow<NotificationCenterEvent>(
        replay = REPLAY_EVENT_COUNT,
    )

    override fun send(event: NotificationCenterEvent) {
        scope.launch(Dispatchers.Main) {
            if (isReplayable(event::class)) {
                replayedEvents.emit(event)
            } else {
                events.emit(event)
            }
        }
    }

    override fun <T : NotificationCenterEvent> subscribe(
        clazz: KClass<T>,
    ): Flow<T> {
        return if (isReplayable(clazz)) {
            replayedEvents.mapNotNull { clazz.safeCast(it) }
        } else {
            events.mapNotNull { clazz.safeCast(it) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun resetCache() {
        replayedEvents.resetReplayCache()
    }
}

private fun <T : NotificationCenterEvent> isReplayable(clazz: KClass<T>): Boolean {
    return when (clazz) {
        NotificationCenterEvent.MultiCommunityCreated::class -> true
        NotificationCenterEvent.PostUpdated::class -> true
        NotificationCenterEvent.PostCreated::class -> true
        NotificationCenterEvent.PostDeleted::class -> true
        NotificationCenterEvent.CommentCreated::class -> true
        NotificationCenterEvent.UserBannedPost::class -> true
        NotificationCenterEvent.UserBannedComment::class -> true
        else -> false
    }
}
