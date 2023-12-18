package com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.di

import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageMviModel
import org.koin.java.KoinJavaComponent

actual fun getZoomableImageViewModel(): ZoomableImageMviModel {
    val res: ZoomableImageMviModel by KoinJavaComponent.inject(ZoomableImageMviModel::class.java)
    return res
}
