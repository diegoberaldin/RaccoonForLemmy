package com.github.diegoberaldin.raccoonforlemmy.unit.userinfo

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Stable
interface UserInfoMviModel :
    MviModel<UserInfoMviModel.Intent, UserInfoMviModel.UiState, UserInfoMviModel.Effect>,
    ScreenModel {

    sealed interface Intent

    data class UiState(
        val user: UserModel = UserModel(),
        val autoLoadImages: Boolean = true,
        val moderatedCommunities: List<CommunityModel> = emptyList(),
    )

    sealed interface Effect
}

