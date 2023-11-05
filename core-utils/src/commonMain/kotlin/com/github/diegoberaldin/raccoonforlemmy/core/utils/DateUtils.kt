package com.github.diegoberaldin.raccoonforlemmy.core.utils

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun String.prettifyDate(): String = let {
    when {
        it.isEmpty() -> it
        !it.endsWith("Z") -> {
            DateTime.getPrettyDate(
                iso8601Timestamp = it + "Z",
                yearLabel = stringResource(
                    MR.strings.profile_year_short
                ),
                monthLabel = stringResource(
                    MR.strings.profile_month_short
                ),
                dayLabel = stringResource(MR.strings.profile_day_short),
                hourLabel = stringResource(
                    MR.strings.post_hour_short
                ),
                minuteLabel = stringResource(
                    MR.strings.post_minute_short
                ),
                secondLabel = stringResource(
                    MR.strings.post_second_short
                ),
            )
        }

        else -> {
            DateTime.getPrettyDate(
                iso8601Timestamp = it,
                yearLabel = stringResource(
                    MR.strings.profile_year_short
                ),
                monthLabel = stringResource(
                    MR.strings.profile_month_short
                ),
                dayLabel = stringResource(MR.strings.profile_day_short),
                hourLabel = stringResource(
                    MR.strings.post_hour_short
                ),
                minuteLabel = stringResource(
                    MR.strings.post_minute_short
                ),
                secondLabel = stringResource(
                    MR.strings.post_second_short
                ),
            )
        }
    }
}