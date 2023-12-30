package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElderlyWoman
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MarkUnreadChatAlt
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

sealed interface SortType {
    data object Active : SortType
    data object Hot : SortType
    data object New : SortType
    data object MostComments : SortType
    data object NewComments : SortType
    data object Old : SortType
    sealed interface Top : SortType {
        data object Generic : Top
        data object PastHour : Top
        data object Past6Hours : Top
        data object Past12Hours : Top
        data object Day : Top
        data object Week : Top
        data object Month : Top
        data object Year : Top
    }

    data object Controversial : SortType
    data object Scaled : SortType
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
    SortType.Controversial -> 14
    SortType.Scaled -> 15
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
    14 -> SortType.Controversial
    15 -> SortType.Scaled
    else -> SortType.Active
}

fun SortType.toIcon(): ImageVector = when (this) {
    SortType.Active -> Icons.Default.RocketLaunch
    SortType.Hot -> Icons.Default.LocalFireDepartment
    SortType.MostComments -> Icons.Default.Forum
    SortType.New -> Icons.Default.TrendingUp
    SortType.NewComments -> Icons.Default.MarkUnreadChatAlt
    SortType.Old -> Icons.Default.ElderlyWoman
    SortType.Controversial -> Icons.Default.Thunderstorm
    SortType.Scaled -> Icons.Default.MonitorWeight
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
    SortType.Controversial -> stringResource(MR.strings.home_sort_type_controversial)
    SortType.Scaled -> stringResource(MR.strings.home_sort_type_scaled)
    else -> stringResource(MR.strings.home_sort_type_top)
}

@Composable
fun SortType?.getAdditionalLabel(): String = when (this) {
    SortType.Top.Day -> stringResource(MR.strings.home_sort_type_top_day_short)
    SortType.Top.Month -> stringResource(MR.strings.home_sort_type_top_month_short)
    SortType.Top.Past12Hours -> stringResource(MR.strings.home_sort_type_top_12_hours_short)
    SortType.Top.Past6Hours -> stringResource(MR.strings.home_sort_type_top_6_hours_short)
    SortType.Top.PastHour -> stringResource(MR.strings.home_sort_type_top_hour_short)
    SortType.Top.Week -> stringResource(MR.strings.home_sort_type_top_week_short)
    SortType.Top.Year -> stringResource(MR.strings.home_sort_type_top_year_short)
    else -> ""
}
