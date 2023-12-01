package com.github.diegoberaldin.raccoonforlemmy.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.fontFamilyResource
import dev.icerock.moko.resources.compose.stringResource
import io.ktor.utils.io.core.toByteArray
import org.kotlincrypto.hash.md.MD5
import kotlin.math.round

@Composable
fun String.toLanguageName() = when (this) {
    "ar" -> stringResource(MR.strings.language_ar)
    "bg" -> stringResource(MR.strings.language_bg)
    "cs" -> stringResource(MR.strings.language_cs)
    "da" -> stringResource(MR.strings.language_da)
    "de" -> stringResource(MR.strings.language_de)
    "el" -> stringResource(MR.strings.language_el)
    "eo" -> stringResource(MR.strings.language_eo)
    "es" -> stringResource(MR.strings.language_es)
    "et" -> stringResource(MR.strings.language_et)
    "ga" -> stringResource(MR.strings.language_ga)
    "fi" -> stringResource(MR.strings.language_fi)
    "fr" -> stringResource(MR.strings.language_fr)
    "hu" -> stringResource(MR.strings.language_hu)
    "hr" -> stringResource(MR.strings.language_hr)
    "it" -> stringResource(MR.strings.language_it)
    "lt" -> stringResource(MR.strings.language_lt)
    "lv" -> stringResource(MR.strings.language_lv)
    "mt" -> stringResource(MR.strings.language_mt)
    "no" -> stringResource(MR.strings.language_no)
    "nl" -> stringResource(MR.strings.language_nl)
    "pl" -> stringResource(MR.strings.language_pl)
    "pt" -> stringResource(MR.strings.language_pt)
    "ro" -> stringResource(MR.strings.language_ro)
    "se" -> stringResource(MR.strings.language_se)
    "sk" -> stringResource(MR.strings.language_sk)
    "sl" -> stringResource(MR.strings.language_sl)
    "tr" -> stringResource(MR.strings.language_tr)
    else -> stringResource(MR.strings.language_en)
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
    "ro" -> "🇷🇴"
    "se" -> "🇸🇪"
    "sk" -> "🇸🇰"
    "sl" -> "🇸🇮"
    "tr" -> "🇹🇷"
    else -> ""
}.let {
    AnnotatedString(
        text = it,
        spanStyle = SpanStyle(fontFamily = fontFamilyResource(MR.fonts.TitilliumWeb.regular))
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
