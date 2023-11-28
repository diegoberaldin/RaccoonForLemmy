package com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer

import androidx.compose.runtime.Stable
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import kotlinx.coroutines.flow.SharedFlow

sealed interface DrawerEvent {
    data object Toggled : DrawerEvent
    data class OpenCommunity(val community: CommunityModel) : DrawerEvent
    data class OpenMultiCommunity(val community: MultiCommunityModel) : DrawerEvent
    data object ManageSubscriptions : DrawerEvent
    data object OpenBookmarks : DrawerEvent
}

@Stable
interface DrawerCoordinator {
    val toggleEvents: SharedFlow<DrawerEvent>
    suspend fun toggleDrawer()

    suspend fun sendEvent(event: DrawerEvent)
}
