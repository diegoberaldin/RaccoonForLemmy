package com.github.diegoberaldin.raccoonforlemmy.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.Locales
import io.ktor.utils.io.core.*
import org.kotlincrypto.hash.md.MD5
import kotlin.math.round

@Composable
fun String.toLanguageName() = when (this) {
    Locales.Ar -> "العربية"
    Locales.Bg -> "български"
    Locales.Cs -> "Čeština"
    Locales.Da -> "Dansk"
    Locales.De -> "Deutsch"
    Locales.Ga -> "Gaeilge"
    Locales.El -> "Ελληνικά"
    Locales.Eo -> "Esperanto"
    Locales.Es -> "Español"
    Locales.Et -> "Eesti"
    Locales.Fi -> "Suomi"
    Locales.Fr -> "Français"
    Locales.Hr -> "Hrvatski"
    Locales.Hu -> "Magyar"
    Locales.It -> "Italiano"
    Locales.Lt -> "Lietuvių"
    Locales.Lv -> "Latviešu"
    Locales.Mt -> "Malti"
    Locales.No -> "Norsk"
    Locales.Nl -> "Nederlands"
    Locales.Pl -> "Polski"
    Locales.Pt -> "Português"
    Locales.PtBr -> "Português (Brazil)"
    Locales.Ro -> "Română"
    Locales.Ru -> "Русский"
    Locales.Se -> "Svenska"
    Locales.Sk -> "Slovenčina"
    Locales.Sl -> "Slovenščina"
    Locales.Sq -> "Shqip"
    Locales.Sr -> "Српски"
    Locales.Tok -> "toki pona"
    Locales.Tr -> "Türkçe"
    Locales.Uk -> "Українська"
    else -> "English"
}

@Composable
fun String.toLanguageFlag(): AnnotatedString = when (this) {
    Locales.Ar -> "🇸🇦"
    Locales.Bg -> "🇧🇬"
    Locales.Cs -> "🇨🇿"
    Locales.Da -> "🇩🇰"
    Locales.De -> "🇩🇪"
    Locales.El -> "🇬🇷"
    Locales.En -> "🇬🇧"
    Locales.Eo -> "🍀"
    Locales.Es -> "🇪🇸"
    Locales.Et -> "🇪🇪"
    Locales.Ga -> "🇮🇪"
    Locales.Fi -> "🇫🇮"
    Locales.Fr -> "🇫🇷"
    Locales.Hu -> "🇭🇺"
    Locales.Hr -> "🇭🇷"
    Locales.It -> "🇮🇹"
    Locales.Lt -> "🇱🇹"
    Locales.Lv -> "🇱🇻"
    Locales.Mt -> "🇲🇹"
    Locales.No -> "🇳🇴"
    Locales.Nl -> "🇳🇱"
    Locales.Pl -> "🇵🇱"
    Locales.Pt -> "🇵🇹"
    Locales.PtBr -> "🇧🇷️"
    Locales.Ro -> "🇷🇴"
    Locales.Ru -> "🇷🇺"
    Locales.Se -> "🇸🇪"
    Locales.Sk -> "🇸🇰"
    Locales.Sl -> "🇸🇮"
    Locales.Sq -> "🇦🇱"
    Locales.Sr -> "🇷🇸️"
    Locales.Tok -> "🦝️"
    Locales.Tr -> "🇹🇷"
    Locales.Uk -> "🇺🇦"
    else -> ""
}.let {
    AnnotatedString(
        text = it,
        spanStyle = SpanStyle(fontFamily = FontFamily.Default),
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
        val extensions = listOf(".jpeg", ".jpg", ".png", ".webp", ".gif")
        return extensions.any { this.endsWith(it) }
    }

val String.looksLikeAVideo: Boolean
    get() {
        val extensions = listOf(".mp4", ".mov", ".webm", ".avi")
        return extensions.any { this.endsWith(it) }
    }

val String.showInEmbeddedWebView: Boolean
    get() {
        val patterns = listOf(
            ".redgifs.com/",
        )
        return patterns.any { this.contains(it) }
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
