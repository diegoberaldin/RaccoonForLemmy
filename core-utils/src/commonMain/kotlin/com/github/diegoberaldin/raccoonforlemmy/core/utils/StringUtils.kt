package com.github.diegoberaldin.raccoonforlemmy.core.utils

expect object StringUtils {
    fun String.isValidUrl(): Boolean
    fun Int.toHexDigit(): String
}

val String.looksLikeAnImage: Boolean
    get() {
        val imageExtensions = listOf(".jpeg", ".jpg", ".png", ".webp", ".gif")
        return imageExtensions.any { this.endsWith(it) }
    }