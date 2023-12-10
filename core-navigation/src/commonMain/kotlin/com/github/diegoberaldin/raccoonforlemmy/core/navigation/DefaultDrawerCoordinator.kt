package com.github.diegoberaldin.raccoonforlemmy.core.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DefaultDrawerCoordinator : DrawerCoordinator {

    override val events = MutableSharedFlow<DrawerEvent>()
    override val gesturesEnabled = MutableStateFlow(true)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun toggleDrawer() {
        scope.launch {
            events.emit(DrawerEvent.Toggled)
        }
    }

    override fun sendEvent(event: DrawerEvent) {
        scope.launch {
            events.emit(event)
        }
    }

    override fun setGesturesEnabled(value: Boolean) {
        gesturesEnabled.value = value
    }
}
