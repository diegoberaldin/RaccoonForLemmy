package com.github.diegoberaldin.racconforlemmy.core.utils

import org.koin.core.module.Module


interface GalleryHelper {
    fun saveToGallery(bytes: ByteArray, name: String)
}

expect val galleryHelperModule: Module
