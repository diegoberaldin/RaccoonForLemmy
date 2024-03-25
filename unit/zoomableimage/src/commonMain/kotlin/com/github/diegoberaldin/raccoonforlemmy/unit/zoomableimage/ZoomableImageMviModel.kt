package com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

@Stable
interface ZoomableImageMviModel :
    ScreenModel,
    MviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect> {
    sealed interface Intent {
        data class SaveToGallery(
            val source: String,
            val url: String,
        ) : Intent
    }

    data class UiState(
        val loading: Boolean = false,
        val autoLoadImages: Boolean = true,
    )

    sealed interface Effect {
        data object ShareSuccess : Effect
        data object ShareFailure : Effect
    }
}