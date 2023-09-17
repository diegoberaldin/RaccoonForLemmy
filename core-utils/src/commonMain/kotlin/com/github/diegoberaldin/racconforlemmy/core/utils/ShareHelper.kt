package com.github.diegoberaldin.racconforlemmy.core.utils

import org.koin.core.module.Module

interface ShareHelper {
    fun shareImage(url: String, mimeType: String)
}

expect val shareHelperModule: Module