package com.github.diegoberaldin.raccoonforlemmy.core.commonui.image

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

class ZoomableImageViewModel(
    private val mvi: DefaultMviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect>,
    private val shareHelper: ShareHelper,
) : ScreenModel,
    MviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect> by mvi {

    override fun reduce(intent: ZoomableImageMviModel.Intent) {
        when (intent) {
            is ZoomableImageMviModel.Intent.Share -> {
                shareHelper.shareImage(intent.url, "image/*")
            }
        }
    }
}