package com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import dev.icerock.moko.resources.desc.StringDesc

@Stable
interface ModalDrawerMviModel :
    MviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data object Refresh : Intent
        data class ChangeInstanceName(val value: String) : Intent
        data object SubmitChangeInstance : Intent
    }

    data class UiState(
        val user: UserModel? = null,
        val autoLoadImages: Boolean = true,
        val refreshing: Boolean = false,
        val instance: String? = null,
        val communities: List<CommunityModel> = emptyList(),
        val multiCommunities: List<MultiCommunityModel> = emptyList(),
        val changeInstanceloading: Boolean = false,
        val changeInstanceName: String = "",
        val changeInstanceNameError: StringDesc? = null,
    )

    sealed interface Effect {
        data object CloseChangeInstanceDialog : Effect
    }
}