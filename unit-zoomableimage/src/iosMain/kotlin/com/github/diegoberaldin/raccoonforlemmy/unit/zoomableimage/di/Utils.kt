package com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.di

import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getZoomableImageViewModel(): ZoomableImageMviModel =
    UnitZoomableImageDiHelper.zoomableImageModel

object UnitZoomableImageDiHelper : KoinComponent {
    val zoomableImageModel: ZoomableImageMviModel by inject()
}