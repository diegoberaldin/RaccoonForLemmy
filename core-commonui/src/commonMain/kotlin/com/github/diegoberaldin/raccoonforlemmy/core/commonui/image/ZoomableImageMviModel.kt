package com.github.diegoberaldin.raccoonforlemmy.core.commonui.image

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface ZoomableImageMviModel :
    MviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect> {
    sealed interface Intent {
        data class Share(val url: String) : Intent
        data class SaveToGallery(val url: String) : Intent
    }

    data class UiState(val loading: Boolean = false)

    sealed interface Effect
}