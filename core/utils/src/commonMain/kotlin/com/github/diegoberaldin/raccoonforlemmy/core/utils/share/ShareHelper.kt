package com.github.diegoberaldin.raccoonforlemmy.core.utils.share

import androidx.compose.runtime.Stable
import org.koin.core.module.Module

@Stable
interface ShareHelper {
    fun share(url: String, mimeType: String = "text/plain")
}

expect val shareHelperModule: Module

expect fun getShareHelper(): ShareHelper