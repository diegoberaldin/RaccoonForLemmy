package com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface DrawerEvent {
    data object Toggled : DrawerEvent
    data class OpenCommunity(val community: CommunityModel) : DrawerEvent
    data class OpenMultiCommunity(val community: MultiCommunityModel) : DrawerEvent
    data object ManageSubscriptions : DrawerEvent
    data object OpenBookmarks : DrawerEvent
}

interface DrawerCoordinator {

    val gesturesEnabled: StateFlow<Boolean>

    val toggleEvents: SharedFlow<DrawerEvent>
    suspend fun toggleDrawer()

    suspend fun sendEvent(event: DrawerEvent)

    fun setGesturesEnabled(value: Boolean)
}
