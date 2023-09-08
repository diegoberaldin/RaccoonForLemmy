package com.github.diegoberaldin.raccoonforlemmy.feature.search.communitylist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun CommunityTopBar(
    listingType: ListingType,
    sortType: SortType,
    onSelectListingType: () -> Unit,
    onSelectSortType: () -> Unit,
) {
    Row(
        modifier = Modifier.height(50.dp).padding(horizontal = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.onClick {
                onSelectListingType()
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Image(
                imageVector = listingType.toIcon(),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Text(
                    text = stringResource(MR.strings.instance_detail_communities),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = listingType.toReadableName(),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row {
                val additionalLabel = when (sortType) {
                    SortType.Top.Day -> stringResource(MR.strings.home_sort_type_top_day_short)
                    SortType.Top.Month -> stringResource(MR.strings.home_sort_type_top_month_short)
                    SortType.Top.Past12Hours -> stringResource(MR.strings.home_sort_type_top_12_hours_short)
                    SortType.Top.Past6Hours -> stringResource(MR.strings.home_sort_type_top_6_hours_short)
                    SortType.Top.PastHour -> stringResource(MR.strings.home_sort_type_top_hour_short)
                    SortType.Top.Week -> stringResource(MR.strings.home_sort_type_top_week_short)
                    SortType.Top.Year -> stringResource(MR.strings.home_sort_type_top_year_short)
                    else -> ""
                }
                if (additionalLabel.isNotEmpty()) {
                    Text(
                        text = buildString {
                            append("(")
                            append(additionalLabel)
                            append(")")
                        }
                    )
                }
                Image(
                    modifier = Modifier.onClick {
                        onSelectSortType()
                    },
                    imageVector = sortType.toIcon(),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                )
            }
        }
    }
}