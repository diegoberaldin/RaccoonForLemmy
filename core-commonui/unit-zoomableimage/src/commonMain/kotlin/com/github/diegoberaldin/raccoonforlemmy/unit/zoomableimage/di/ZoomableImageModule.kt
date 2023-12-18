package com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageViewModel
import org.koin.dsl.module

val zoomableImageModule = module {
    factory<ZoomableImageMviModel> {
        ZoomableImageViewModel(
            mvi = DefaultMviModel(ZoomableImageMviModel.UiState()),
            shareHelper = get(),
            galleryHelper = get(),
            settingsRepository = get(),
        )
    }
}