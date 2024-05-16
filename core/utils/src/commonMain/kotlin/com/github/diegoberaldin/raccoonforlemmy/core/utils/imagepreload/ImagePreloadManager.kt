package com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload

interface ImagePreloadManager {
    fun preload(url: String)

    fun remove(url: String)
}
