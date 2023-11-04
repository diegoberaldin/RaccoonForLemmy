package com.github.diegoberaldin.raccoonforlemmy.core.commonui.image

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

@Stable
interface ZoomableImageMviModel :
    MviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class Share(val url: String) : Intent
        data class SaveToGallery(val url: String) : Intent
    }

    data class UiState(
        val loading: Boolean = false,
        val autoLoadImages: Boolean = true,
    )

    sealed interface Effect {
        data object ShareSuccess : Effect
    }
}