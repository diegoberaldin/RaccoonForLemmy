package com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage

import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.ContentScale
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

@Stable
interface ZoomableImageMviModel :
    ScreenModel,
    MviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect> {
    sealed interface Intent {
        data class SaveToGallery(
            val source: String,
        ) : Intent

        data class ChangeContentScale(val contentScale: ContentScale) : Intent
    }

    data class UiState(
        val loading: Boolean = false,
        val autoLoadImages: Boolean = true,
        val contentScale: ContentScale = ContentScale.FillWidth,
    )

    sealed interface Effect {
        data object ShareSuccess : Effect
        data object ShareFailure : Effect
    }
}
