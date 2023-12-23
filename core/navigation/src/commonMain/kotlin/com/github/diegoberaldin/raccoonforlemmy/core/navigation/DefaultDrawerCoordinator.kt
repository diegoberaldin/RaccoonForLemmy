package com.github.diegoberaldin.raccoonforlemmy.core.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultDrawerCoordinator : DrawerCoordinator {

    override val events = MutableSharedFlow<DrawerEvent>()
    override val gesturesEnabled = MutableStateFlow(true)

    override suspend fun toggleDrawer() {
        events.emit(DrawerEvent.Toggled)
    }

    override suspend fun sendEvent(event: DrawerEvent) {
        events.emit(event)
    }

    override fun setGesturesEnabled(value: Boolean) {
        gesturesEnabled.value = value
    }
}
