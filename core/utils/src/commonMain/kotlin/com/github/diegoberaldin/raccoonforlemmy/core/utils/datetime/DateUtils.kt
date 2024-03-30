package com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import kotlin.math.round
import kotlin.time.Duration

@Composable
fun String.prettifyDate(): String = let {
    when {
        it.isEmpty() -> it
        !it.endsWith("Z") -> {
            getPrettyDate(
                iso8601Timestamp = it + "Z",
                yearLabel = LocalXmlStrings.current.profileYearShort,
                monthLabel = LocalXmlStrings.current.profileMonthShort,
                dayLabel = LocalXmlStrings.current.profileDayShort,
                hourLabel = LocalXmlStrings.current.postHourShort,
                minuteLabel = LocalXmlStrings.current.postMinuteShort,
                secondLabel = LocalXmlStrings.current.postSecondShort,
            )
        }

        else -> {
            getPrettyDate(
                iso8601Timestamp = it,
                yearLabel = LocalXmlStrings.current.profileYearShort,
                monthLabel = LocalXmlStrings.current.profileMonthShort,
                dayLabel = LocalXmlStrings.current.profileDayShort,
                hourLabel = LocalXmlStrings.current.postHourShort,
                minuteLabel = LocalXmlStrings.current.postMinuteShort,
                secondLabel = LocalXmlStrings.current.postSecondShort,
            )
        }
    }
}

fun Duration.getPrettyDuration(
    secondsLabel: String,
    minutesLabel: String,
    hoursLabel: String,
): String = when {
    inWholeHours > 0 -> buildString {
        append(inWholeHours)
        append(hoursLabel)
        val remainderMinutes = inWholeMinutes % 60
        val remainderSeconds = inWholeSeconds % 60
        if (remainderMinutes > 0 || remainderSeconds > 0) {
            append(" ")
            append(remainderMinutes)
            append(minutesLabel)
        }
        if (remainderSeconds > 0) {
            append(" ")
            append(remainderSeconds)
            append(secondsLabel)
        }
    }

    inWholeMinutes > 0 -> buildString {
        append(inWholeMinutes)
        append(minutesLabel)
        val remainderSeconds = inWholeSeconds % 60
        if (remainderSeconds > 0) {
            append(" ")
            append(remainderSeconds)
            append(secondsLabel)
        }
    }

    else -> buildString {
        val rounded = round((inWholeMilliseconds / 1000.0) * 10.0) / 10.0
        if (rounded % 1 <= 0) {
            append(rounded.toInt())
        } else {
            append(rounded)
        }
        append(secondsLabel)
    }
}
