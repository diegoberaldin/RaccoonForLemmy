package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object DefaultNotificationCenter : NotificationCenter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override val events = MutableSharedFlow<NotificationCenter.Event>()
    private val registry = mutableMapOf<Pair<String, String>, (Any) -> Unit>()

    override fun send(event: NotificationCenter.Event) {
        scope.launch(Dispatchers.Main) {
            events.emit(event)
        }
    }

    override fun addObserver(observer: (Any) -> Unit, key: String, contract: String) {
        val mapKey = key to contract
        registry[mapKey] = observer
    }

    override fun getObserver(contract: String): ((Any) -> Unit)? {
        return registry.filter { it.key.second == contract }.map { it.value }.firstOrNull()
    }

    override fun removeObserver(key: String) {
        val keysToRemove = registry.keys.filter { it.first == key }
        for (key in keysToRemove) {
            registry.remove(key)
        }
    }
}
