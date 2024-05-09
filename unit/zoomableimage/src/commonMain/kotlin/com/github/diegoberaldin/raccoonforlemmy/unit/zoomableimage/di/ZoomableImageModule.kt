package com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.di

import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageViewModel
import org.koin.dsl.module

val zoomableImageModule = module {
    factory<ZoomableImageMviModel> { params ->
        ZoomableImageViewModel(
            url = params[0],
            shareHelper = get(),
            galleryHelper = get(),
            settingsRepository = get(),
            notificationCenter = get(),
            imagePreloadManager = get(),
        )
    }
}
