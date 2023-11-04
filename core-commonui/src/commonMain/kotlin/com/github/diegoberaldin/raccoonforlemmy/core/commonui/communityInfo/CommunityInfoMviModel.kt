package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

@Stable
interface CommunityInfoMviModel :
    MviModel<CommunityInfoMviModel.Intent, CommunityInfoMviModel.UiState, CommunityInfoMviModel.Effect>,
    ScreenModel {

    sealed interface Intent

    data class UiState(
        val community: CommunityModel = CommunityModel(),
    )

    sealed interface Effect
}
