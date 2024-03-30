package com.github.diegoberaldin.raccoonforlemmy.core.utils.share

import androidx.compose.runtime.Stable
import org.koin.core.module.Module

@Stable
interface ShareHelper {
    val supportsShareImage: Boolean
    fun share(url: String, mimeType: String = "text/plain")
    fun shareImage(path: Any?, mimeType: String = "image/*")
}

expect val shareHelperModule: Module

expect fun getShareHelper(): ShareHelper
