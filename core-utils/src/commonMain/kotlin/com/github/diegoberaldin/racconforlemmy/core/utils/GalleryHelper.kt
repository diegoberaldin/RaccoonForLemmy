package com.github.diegoberaldin.racconforlemmy.core.utils

import androidx.compose.runtime.Composable
import org.koin.core.module.Module


interface GalleryHelper {
    fun saveToGallery(bytes: ByteArray, name: String)

    @Composable
    fun getImageFromGallery(result: (ByteArray) -> Unit)
}

expect val galleryHelperModule: Module

expect fun getGalleryHelper(): GalleryHelper