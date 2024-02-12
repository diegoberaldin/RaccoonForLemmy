package com.github.diegoberaldin.raccoonforlemmy.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import io.ktor.utils.io.core.toByteArray
import org.kotlincrypto.hash.md.MD5
import kotlin.math.round

/*

































 */

@Composable
fun String.toLanguageName() = when (this) {
    "ar" -> "العربية"
    "bg" -> "български"
    "cs" -> "Čeština"
    "da" -> "Dansk"
    "de" -> "Deutsch"
    "ga" -> "Gaeilge"
    "el" -> "Ελληνικά"
    "eo" -> "Esperanto"
    "et" -> "Español"
    "es" -> "Eesti"
    "fi" -> "Suomi"
    "fr" -> "Français"
    "hr" -> "Hrvatski"
    "hu" -> "Magyar"
    "it" -> "Italiano"
    "lt" -> "Lietuvių"
    "lv" -> "Latviešu"
    "mt" -> "Malti"
    "no" -> "Norsk"
    "nl" -> "Nederlands"
    "pl" -> "Polski"
    "pt" -> "Português"
    "pt-BR" -> "Português (Brazil)"
    "ro" -> "Română"
    "ru" -> "Русский"
    "se" -> "Svenska"
    "sk" -> "Slovenčina"
    "sl" -> "Slovenščina"
    "sq" -> "Shqip"
    "tok" -> "toki pona"
    "tr" -> "Türkçe"
    "uk" -> "Українська"
    else -> "English"
}

@Composable
fun String.toLanguageFlag(): AnnotatedString = when (this) {
    "ar" -> "🇸🇦"
    "bg" -> "🇧🇬"
    "cs" -> "🇨🇿"
    "da" -> "🇩🇰"
    "de" -> "🇩🇪"
    "el" -> "🇬🇷"
    "en" -> "🇬🇧"
    "eo" -> "🍀"
    "es" -> "🇪🇸"
    "et" -> "🇪🇪"
    "ga" -> "🇮🇪"
    "fi" -> "🇫🇮"
    "fr" -> "🇫🇷"
    "hu" -> "🇭🇺"
    "hr" -> "🇭🇷"
    "it" -> "🇮🇹"
    "lt" -> "🇱🇹"
    "lv" -> "🇱🇻"
    "mt" -> "🇲🇹"
    "no" -> "🇳🇴"
    "nl" -> "🇳🇱"
    "pl" -> "🇵🇱"
    "pt" -> "🇵🇹"
    "pt-BR" -> "🇧🇷️"
    "ro" -> "🇷🇴"
    "ru" -> "🇷🇺"
    "se" -> "🇸🇪"
    "sk" -> "🇸🇰"
    "sl" -> "🇸🇮"
    "sq" -> "🇦🇱"
    "tok" -> "🦝️"
    "tr" -> "🇹🇷"
    "uk" -> "🇺🇦"
    else -> ""
}.let {
    AnnotatedString(
        text = it,
        spanStyle = SpanStyle(fontFamily = FontFamily.Default)
    )
}

fun String.toLanguageDirection(): LayoutDirection = when (this) {
    "ar" -> LayoutDirection.Rtl
    else -> LayoutDirection.Ltr
}

@Composable
fun Dp.toLocalPixel(): Float = with(LocalDensity.current) {
    value * density
}

@Composable
fun Float.toLocalDp(): Dp = with(LocalDensity.current) {
    this@toLocalDp.toDp()
}

fun Int.getPrettyNumber(
    millionLabel: String,
    thousandLabel: String,
): String {
    val value = this
    return when {
        value > 1_000_000 -> buildString {
            val rounded = round((value / 1_000_000.0) * 10) / 10
            if (rounded % 1 <= 0) {
                append(rounded.toInt())
            } else {
                append(rounded)
            }
            append(millionLabel)
        }

        value > 1_000 -> buildString {
            val rounded = round((value / 1_000.0) * 10) / 10
            if (rounded % 1 <= 0) {
                append(rounded.toInt())
            } else {
                append(rounded)
            }
            append(thousandLabel)
        }

        else -> buildString {
            append(value)
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun String.md5(): String {
    val digest = MD5()
    val bytes = digest.digest(toByteArray())
    return bytes.joinToString {
        it.toHexString()
    }
}

fun Int.toInboxUnreadOnly(): Boolean = this == 0

fun Boolean.toInboxDefaultType(): Int = if (this) 0 else 1

val String.looksLikeAnImage: Boolean
    get() {
        val imageExtensions = listOf(".jpeg", ".jpg", ".png", ".webp", ".gif")
        return imageExtensions.any { this.endsWith(it) }
    }

val String.looksLikeAVideo: Boolean
    get() {
        val imageExtensions = listOf(".mp4", ".mov", ".webm", ".avi")
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
