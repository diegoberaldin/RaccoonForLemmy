package com.github.diegoberaldin.raccoonforlemmy.core.commonui

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ImagePreloadManager

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