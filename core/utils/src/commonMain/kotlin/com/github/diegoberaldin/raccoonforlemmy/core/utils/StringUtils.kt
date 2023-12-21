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

fun String?.ellipsize(length: Int = 100, ellipsis: String = "…"): String {
    if (isNullOrEmpty() || length == 0) {
        return ""
    }
    if (this.length < length) {
        return this
    }
    return take(length - 1) + ellipsis
}