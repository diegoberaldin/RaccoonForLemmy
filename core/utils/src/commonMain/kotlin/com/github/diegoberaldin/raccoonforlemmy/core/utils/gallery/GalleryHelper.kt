package com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import org.koin.core.module.Module


@Stable
interface GalleryHelper {
    fun saveToGallery(bytes: ByteArray, name: String)

    @Composable
    fun getImageFromGallery(result: (ByteArray) -> Unit)
}

expect val galleryHelperModule: Module

expect fun getGalleryHelper(): GalleryHelper