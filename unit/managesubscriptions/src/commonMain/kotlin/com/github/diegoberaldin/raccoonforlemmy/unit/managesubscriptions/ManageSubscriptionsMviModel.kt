package com.github.diegoberaldin.raccoonforlemmy.unit.managesubscriptions

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

@Stable
interface ManageSubscriptionsMviModel :
    MviModel<ManageSubscriptionsMviModel.Intent, ManageSubscriptionsMviModel.UiState, ManageSubscriptionsMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data object Refresh : Intent
        data object HapticIndication : Intent
        data class Unsubscribe(val id: Int) : Intent
        data class DeleteMultiCommunity(val id: Int) : Intent
        data class ToggleFavorite(val id: Int) : Intent
    }

    data class UiState(
        val initial: Boolean = true,
        val refreshing: Boolean = false,
        val multiCommunities: List<MultiCommunityModel> = emptyList(),
        val communities: List<CommunityModel> = emptyList(),
        val autoLoadImages: Boolean = true,
    )

    sealed interface Effect
}