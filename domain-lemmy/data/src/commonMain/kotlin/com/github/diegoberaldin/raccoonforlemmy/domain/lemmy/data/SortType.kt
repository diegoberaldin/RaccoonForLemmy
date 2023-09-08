package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

sealed interface SortType {
    object Active : SortType
    object Hot : SortType
    object New : SortType
    object MostComments : SortType
    object NewComments : SortType
    object Old : SortType
    sealed interface Top : SortType {
        object Generic : Top
        object PastHour : Top
        object Past6Hours : Top
        object Past12Hours : Top
        object Day : Top
        object Week : Top
        object Month : Top
        object Year : Top
    }
}

fun SortType.toInt() = when (this) {
    SortType.Hot -> 1
    SortType.MostComments -> 2
    SortType.New -> 3
    SortType.NewComments -> 4
    SortType.Top.Day -> 5
    SortType.Top.Generic -> 6
    SortType.Top.Month -> 7
    SortType.Top.Past12Hours -> 8
    SortType.Top.Past6Hours -> 9
    SortType.Top.PastHour -> 10
    SortType.Top.Week -> 11
    SortType.Top.Year -> 12
    SortType.Old -> 13
    else -> 0
}

fun Int.toSortType() = when (this) {
    1 -> SortType.Hot
    2 -> SortType.MostComments
    3 -> SortType.New
    4 -> SortType.NewComments
    5 -> SortType.Top.Day
    6 -> SortType.Top.Generic
    7 -> SortType.Top.Month
    8 -> SortType.Top.Past12Hours
    9 -> SortType.Top.Past6Hours
    10 -> SortType.Top.PastHour
    11 -> SortType.Top.Week
    12 -> SortType.Top.Year
    13 -> SortType.Old
    else -> SortType.Active
}

fun SortType.toIcon(): ImageVector = when (this) {
    SortType.Active -> Icons.Default.Rocket
    SortType.Hot -> Icons.Default.Fireplace
    SortType.MostComments -> Icons.Default.Reviews
    SortType.New -> Icons.Default.Bolt
    SortType.NewComments -> Icons.Default.AddComment
    SortType.Old -> Icons.Default.Timer
    else -> Icons.Default.MilitaryTech
}

@Composable
fun SortType.toReadableName(): String = when (this) {
    SortType.Active -> stringResource(MR.strings.home_sort_type_active)
    SortType.Hot -> stringResource(MR.strings.home_sort_type_hot)
    SortType.MostComments -> stringResource(MR.strings.home_sort_type_most_comments)
    SortType.New -> stringResource(MR.strings.home_sort_type_new)
    SortType.NewComments -> stringResource(MR.strings.home_sort_type_new_comments)
    SortType.Top.Day -> stringResource(MR.strings.home_sort_type_top_day)
    SortType.Top.Month -> stringResource(MR.strings.home_sort_type_top_month)
    SortType.Top.Past12Hours -> stringResource(MR.strings.home_sort_type_top_12_hours)
    SortType.Top.Past6Hours -> stringResource(MR.strings.home_sort_type_top_6_hours)
    SortType.Top.PastHour -> stringResource(MR.strings.home_sort_type_top_hour)
    SortType.Top.Week -> stringResource(MR.strings.home_sort_type_top_week)
    SortType.Top.Year -> stringResource(MR.strings.home_sort_type_top_year)
    SortType.Old -> stringResource(MR.strings.home_sort_type_old)
    else -> stringResource(MR.strings.home_sort_type_top)
}
