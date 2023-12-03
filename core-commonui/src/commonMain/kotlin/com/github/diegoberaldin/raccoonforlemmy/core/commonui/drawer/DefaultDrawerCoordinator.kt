package com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class DefaultDrawerCoordinator : DrawerCoordinator {

    override val toggleEvents = MutableSharedFlow<DrawerEvent>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun toggleDrawer() {
        scope.launch {
            toggleEvents.emit(DrawerEvent.Toggled)
        }
    }

    override fun sendEvent(event: DrawerEvent) {
        scope.launch {
            toggleEvents.emit(event)
        }
    }
}
