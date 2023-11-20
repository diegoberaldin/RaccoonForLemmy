package com.github.diegoberaldin.raccoonforlemmy.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlin.math.round

@Composable
fun String.toLanguageName() = when (this) {
    "bg" -> stringResource(MR.strings.language_bg)
    "cs" -> stringResource(MR.strings.language_cs)
    "da" -> stringResource(MR.strings.language_da)
    "de" -> stringResource(MR.strings.language_de)
    "el" -> stringResource(MR.strings.language_el)
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
    "no" -> stringResource(MR.strings.language_no)
    "nl" -> stringResource(MR.strings.language_nl)
    "pl" -> stringResource(MR.strings.language_pl)
    "pt" -> stringResource(MR.strings.language_pt)
    "ro" -> stringResource(MR.strings.language_ro)
    "se" -> stringResource(MR.strings.language_se)
    "sk" -> stringResource(MR.strings.language_sk)
    "sl" -> stringResource(MR.strings.language_sl)
    else -> stringResource(MR.strings.language_en)
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
