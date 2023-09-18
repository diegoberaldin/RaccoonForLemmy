package com.github.diegoberaldin.racconforlemmy.core.utils

import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import org.koin.dsl.module
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum


typealias ImageBytes = NSData

fun ByteArray.toImageBytes(): ImageBytes = memScoped {
    return NSData.create(
        bytes = allocArrayOf(this@toImageBytes),
        length = this@toImageBytes.size.toULong()
    )
}

class DefaultGalleryHelper : GalleryHelper {

    override fun saveToGallery(bytes: ByteArray, name: String) {
        val image = UIImage(bytes.toImageBytes())
        UIImageWriteToSavedPhotosAlbum(image, null, null, null)
    }
}

actual val galleryHelperModule = module {
    single<GalleryHelper> {
        DefaultGalleryHelper()
    }
}
