package com.github.diegoberaldin.raccoonforlemmy.core.navigation

import androidx.compose.runtime.Stable
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface DrawerEvent {
    data object Toggle : DrawerEvent
    data object Close : DrawerEvent
    data class OpenCommunity(val community: CommunityModel) : DrawerEvent
    data class OpenMultiCommunity(val community: MultiCommunityModel) : DrawerEvent
    data class ChangeListingType(val value: ListingType) : DrawerEvent
}

@Stable
interface DrawerCoordinator {
    val events: SharedFlow<DrawerEvent>
    val gesturesEnabled: StateFlow<Boolean>

    suspend fun toggleDrawer()
    suspend fun closeDrawer()
    suspend fun sendEvent(event: DrawerEvent)
    fun setGesturesEnabled(value: Boolean)
}
