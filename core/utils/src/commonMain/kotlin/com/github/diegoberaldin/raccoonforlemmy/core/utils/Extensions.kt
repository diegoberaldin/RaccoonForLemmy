package com.github.diegoberaldin.raccoonforlemmy.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.Locales
import io.ktor.utils.io.core.toByteArray
import org.kotlincrypto.hash.md.MD5
import kotlin.math.round

@Composable
fun String.toLanguageName() =
    when (this) {
        Locales.AR -> "العربية"
        Locales.BG -> "български"
        Locales.CS -> "Čeština"
        Locales.DA -> "Dansk"
        Locales.DE -> "Deutsch"
        Locales.GA -> "Gaeilge"
        Locales.EL -> "Ελληνικά"
        Locales.EO -> "Esperanto"
        Locales.ES -> "Español"
        Locales.ET -> "Eesti"
        Locales.FI -> "Suomi"
        Locales.FR -> "Français"
        Locales.HR -> "Hrvatski"
        Locales.HU -> "Magyar"
        Locales.IT -> "Italiano"
        Locales.LT -> "Lietuvių"
        Locales.LV -> "Latviešu"
        Locales.MT -> "Malti"
        Locales.NB -> "Norsk (Bokmål)"
        Locales.NN -> "Norsk (Nynorsk)"
        Locales.NL -> "Nederlands"
        Locales.PL -> "Polski"
        Locales.PT -> "Português"
        Locales.PT_BR -> "Português (Brazil)"
        Locales.RO -> "Română"
        Locales.RU -> "Русский"
        Locales.SV -> "Svenska"
        Locales.SK -> "Slovenčina"
        Locales.SL -> "Slovenščina"
        Locales.SQ -> "Shqip"
        Locales.SR -> "Српски"
        Locales.TOK -> "toki pona"
        Locales.TR -> "Türkçe"
        Locales.UK -> "Українська"
        Locales.ZH_TW -> "正體中文"
        Locales.ZH_HK -> "廣東話"
        else -> "English"
    }

@Composable
fun String.toLanguageFlag(): AnnotatedString =
    when (this) {
        Locales.AR -> "🇸🇦"
        Locales.BG -> "🇧🇬"
        Locales.CS -> "🇨🇿"
        Locales.DA -> "🇩🇰"
        Locales.DE -> "🇩🇪"
        Locales.EL -> "🇬🇷"
        Locales.EN -> "🇬🇧"
        Locales.EO -> "🍀"
        Locales.ES -> "🇪🇸"
        Locales.ET -> "🇪🇪"
        Locales.GA -> "🇮🇪"
        Locales.FI -> "🇫🇮"
        Locales.FR -> "🇫🇷"
        Locales.HU -> "🇭🇺"
        Locales.HR -> "🇭🇷"
        Locales.IT -> "🇮🇹"
        Locales.LT -> "🇱🇹"
        Locales.LV -> "🇱🇻"
        Locales.MT -> "🇲🇹"
        Locales.NB -> "🇳🇴"
        Locales.NN -> "🇳🇴"
        Locales.NL -> "🇳🇱"
        Locales.PL -> "🇵🇱"
        Locales.PT -> "🇵🇹"
        Locales.PT_BR -> "🇧🇷️"
        Locales.RO -> "🇷🇴"
        Locales.RU -> "🇷🇺"
        Locales.SV -> "🇸🇪"
        Locales.SK -> "🇸🇰"
        Locales.SL -> "🇸🇮"
        Locales.SQ -> "🇦🇱"
        Locales.SR -> "🇷🇸️"
        Locales.TOK -> "🦝️"
        Locales.TR -> "🇹🇷"
        Locales.UK -> "🇺🇦"
        Locales.ZH_TW -> "🇹🇼"
        Locales.ZH_HK -> "🇭🇰"
        else -> ""
    }.let {
        AnnotatedString(
            text = it,
            spanStyle = SpanStyle(fontFamily = FontFamily.Default),
        )
    }

fun String.toLanguageDirection(): LayoutDirection =
    when (this) {
        "ar" -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

@Composable
fun Dp.toLocalPixel(): Float =
    with(LocalDensity.current) {
        value * density
    }

@Composable
fun Float.toLocalDp(): Dp =
    with(LocalDensity.current) {
        this@toLocalDp.toDp()
    }

fun Int.getPrettyNumber(
    millionLabel: String,
    thousandLabel: String,
): String {
    val value = this
    return when {
        value > 1_000_000 ->
            buildString {
                val rounded = round((value / 1_000_000.0) * 10) / 10
                if (rounded % 1 <= 0) {
                    append(rounded.toInt())
                } else {
                    append(rounded)
                }
                append(millionLabel)
            }

        value > 1_000 ->
            buildString {
                val rounded = round((value / 1_000.0) * 10) / 10
                if (rounded % 1 <= 0) {
                    append(rounded.toInt())
                } else {
                    append(rounded)
                }
                append(thousandLabel)
            }

        else ->
            buildString {
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

val String.isRedGifs: Boolean
    get() = contains("redgifs.com")

fun String?.ellipsize(
    length: Int = 100,
    ellipsis: String = "…",
): String {
    if (isNullOrEmpty() || length == 0) {
        return ""
    }
    if (this.length < length) {
        return this
    }
    return take(length - 1) + ellipsis
}
