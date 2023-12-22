package com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache

fun getCoilImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
    .components {
        if (Build.VERSION.SDK_INT >= 28) {
            add(ImageDecoderDecoder.Factory())
        } else {
            add(GifDecoder.Factory())
        }
    }
    .memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.25)
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache"))
            .maxSizePercent(0.02)
            .build()
    }
    .crossfade(true)
    .build()