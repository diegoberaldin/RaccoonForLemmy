package com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.epochMillis
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.GalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.download
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ZoomableImageViewModel(
    private val settingsRepository: SettingsRepository,
    private val shareHelper: ShareHelper,
    private val galleryHelper: GalleryHelper,
) : ZoomableImageMviModel,
    DefaultMviModel<ZoomableImageMviModel.Intent, ZoomableImageMviModel.UiState, ZoomableImageMviModel.Effect>(
        initialState = ZoomableImageMviModel.UiState(),
    ) {

    init {
        screenModelScope.launch {
            settingsRepository.currentSettings.onEach { settings ->
                updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
            }.launchIn(this)
        }
    }

    override fun reduce(intent: ZoomableImageMviModel.Intent) {
        when (intent) {
            is ZoomableImageMviModel.Intent.Share -> {
                runCatching {
                    shareHelper.share(intent.url)
                }
            }

            is ZoomableImageMviModel.Intent.SaveToGallery -> downloadAndSave(
                folder = intent.source,
                url = intent.url,
            )
        }
    }

    private fun downloadAndSave(url: String, folder: String) {
        val imageSourcePath = settingsRepository.currentSettings.value.imageSourcePath
        screenModelScope.launch(Dispatchers.IO) {
            updateState { it.copy(loading = true) }
            try {
                val bytes = galleryHelper.download(url)
                val extension = url.let { s ->
                    val idx = s.lastIndexOf(".").takeIf { it >= 0 } ?: s.length
                    s.substring(idx).takeIf { it.isNotEmpty() } ?: ".jpeg"
                }
                galleryHelper.saveToGallery(
                    bytes = bytes,
                    name = "${epochMillis()}$extension",
                    additionalPathSegment = folder.takeIf { imageSourcePath },
                )
                emitEffect(ZoomableImageMviModel.Effect.ShareSuccess)
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                updateState { it.copy(loading = false) }
            }
        }
    }
}