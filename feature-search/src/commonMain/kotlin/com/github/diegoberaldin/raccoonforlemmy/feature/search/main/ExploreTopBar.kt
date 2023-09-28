package com.github.diegoberaldin.raccoonforlemmy.feature.search.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommunityTopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    listingType: ListingType,
    sortType: SortType,
    isLogged: Boolean = false,
    onSelectListingType: (() -> Unit)? = null,
    onSelectSortType: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.background,
        ),
        navigationIcon = {
            Image(
                modifier = Modifier.onClick {
                    onSelectListingType?.invoke()
                },
                imageVector = listingType.toIcon(),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            )
        },
        title = {
            Column(
                modifier = Modifier.padding(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Text(
                    text = stringResource(MR.strings.navigation_search),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = listingType.toReadableName(),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
        actions = {
            if (isLogged) {
                Image(
                    modifier = Modifier.onClick {
                        onSettings?.invoke()
                    },
                    imageVector = Icons.Default.ManageAccounts,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                )
                Spacer(modifier = Modifier.width(Spacing.s))
            }

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
                Spacer(modifier = Modifier.width(Spacing.xs))
            }

            Image(
                modifier = Modifier.onClick {
                    onSelectSortType?.invoke()
                },
                imageVector = sortType.toIcon(),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            )
        },
    )
}