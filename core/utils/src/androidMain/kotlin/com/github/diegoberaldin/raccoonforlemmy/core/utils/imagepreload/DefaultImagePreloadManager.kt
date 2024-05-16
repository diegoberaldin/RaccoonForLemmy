package com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload

import android.content.Context
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ImageRequest

class DefaultImagePreloadManager(
    private val context: Context,
) : ImagePreloadManager {
    override fun preload(url: String) {
        val request =
            ImageRequest.Builder(context)
                .data(url)
                .build()
        context.imageLoader.enqueue(request)
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun remove(url: String) {
        context.imageLoader.memoryCache?.remove(MemoryCache.Key(url))
        context.imageLoader.diskCache?.remove(url)
    }
}
