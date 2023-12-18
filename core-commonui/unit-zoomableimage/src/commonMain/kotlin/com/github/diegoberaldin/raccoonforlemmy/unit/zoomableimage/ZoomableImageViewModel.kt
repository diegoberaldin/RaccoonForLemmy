package com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.DateTime
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.GalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.download
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ZoomableImageViewModel(
    private val mvi: DefaultMviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect>,
    private val settingsRepository: SettingsRepository,
    private val shareHelper: ShareHelper,
    private val galleryHelper: GalleryHelper,
) : ZoomableImageMviModel,
    MviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
            }.launchIn(this)
        }
    }

    override fun reduce(intent: ZoomableImageMviModel.Intent) {
        when (intent) {
            is ZoomableImageMviModel.Intent.Share -> {
                runCatching {
                    shareHelper.share(intent.url, "image/*")
                }
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
                mvi.emitEffect(ZoomableImageMviModel.Effect.ShareSuccess)
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                mvi.updateState { it.copy(loading = false) }
            }
        }
    }
}