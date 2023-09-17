package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import kotlinx.coroutines.flow.SharedFlow

/**
 * Utility to publish and subscribe for broadcast notifications.
 */
interface NotificationCenter {

    /**
     * Available event types.
     */
    sealed interface Event

    /**
     * Observable event flow
     */
    val events: SharedFlow<Event>

    /**
     * Publish and event to subscribers.
     *
     * @param event Event to send
     */
    fun send(event: Event)

    fun addObserver(observer: (Any) -> Unit, key: String, contract: String)

    fun getObserver(contract: String): ((Any) -> Unit)?

    fun getAllObservers(contract: String): List<(Any) -> Unit>

    fun removeObserver(key: String)
}
