package com.github.diegoberaldin.raccoonforlemmy.core.utils.share

import androidx.compose.runtime.Stable

@Stable
interface ShareHelper {
    val supportsShareImage: Boolean
    fun share(url: String, mimeType: String = "text/plain")
    fun shareImage(path: Any?, mimeType: String = "image/*")
}

expect fun getShareHelper(): ShareHelper
