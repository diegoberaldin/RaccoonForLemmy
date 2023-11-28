package com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer

import kotlinx.coroutines.flow.MutableSharedFlow

class DefaultDrawerCoordinator : DrawerCoordinator {

    override val toggleEvents = MutableSharedFlow<DrawerEvent>()

    override suspend fun toggleDrawer() {
        toggleEvents.emit(DrawerEvent.Toggled)
    }

    override suspend fun sendEvent(event: DrawerEvent) {
        toggleEvents.emit(event)
    }
}
