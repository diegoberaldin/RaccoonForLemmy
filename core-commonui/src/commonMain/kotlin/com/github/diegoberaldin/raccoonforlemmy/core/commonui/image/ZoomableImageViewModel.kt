package com.github.diegoberaldin.raccoonforlemmy.core.commonui.image

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.DateTime
import com.github.diegoberaldin.racconforlemmy.core.utils.GalleryHelper
import com.github.diegoberaldin.racconforlemmy.core.utils.ShareHelper
import com.github.diegoberaldin.racconforlemmy.core.utils.download
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class ZoomableImageViewModel(
    private val mvi: DefaultMviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect>,
    private val shareHelper: ShareHelper,
    private val galleryHelper: GalleryHelper,
) : ScreenModel,
    MviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect> by mvi {

    override fun reduce(intent: ZoomableImageMviModel.Intent) {
        when (intent) {
            is ZoomableImageMviModel.Intent.Share -> {
                shareHelper.share(intent.url, "image/*")
            }

            is ZoomableImageMviModel.Intent.SaveToGallery -> downloadAndSave(intent.url)
        }
    }

    private fun downloadAndSave(url: String) {
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            try {
                val bytes = galleryHelper.download(url)
                val extension = url.let { s ->
                    val idx = s.lastIndexOf(".").takeIf { it >= 0 } ?: s.length
                    s.substring(idx).takeIf { it.isNotEmpty() } ?: ".jpeg"
                }
                galleryHelper.saveToGallery(bytes, "${DateTime.epochMillis()}.$extension")
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                mvi.updateState { it.copy(loading = false) }
            }
        }
    }
}