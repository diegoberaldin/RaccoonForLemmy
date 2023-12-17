package com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest

class DefaultImagePreloadManager(
    private val context: Context,
) : ImagePreloadManager {

    override fun preload(url: String) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()
        context.imageLoader.enqueue(request)
    }
}