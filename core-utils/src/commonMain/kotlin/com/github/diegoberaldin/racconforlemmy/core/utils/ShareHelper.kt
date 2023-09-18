package com.github.diegoberaldin.racconforlemmy.core.utils

import org.koin.core.module.Module

interface ShareHelper {
    fun share(url: String, mimeType: String)
}

expect val shareHelperModule: Module