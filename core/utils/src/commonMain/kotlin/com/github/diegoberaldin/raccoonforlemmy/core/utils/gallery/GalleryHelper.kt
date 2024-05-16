package com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
interface GalleryHelper {
    val supportsCustomPath: Boolean

    fun saveToGallery(
        bytes: ByteArray,
        name: String,
        additionalPathSegment: String? = null,
    ): Any?

    @Composable
    fun getImageFromGallery(result: (ByteArray) -> Unit)
}

expect fun getGalleryHelper(): GalleryHelper
