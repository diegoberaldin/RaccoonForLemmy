package com.github.diegoberaldin.raccoonforlemmy.feature.search.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExploreTopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    listingType: ListingType,
    sortType: SortType,
    onSelectListingType: (() -> Unit)? = null,
    onSelectSortType: (() -> Unit)? = null,
    onHamburgerTapped: (() -> Unit)? = null,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            when {
                onHamburgerTapped != null -> {
                    Image(
                        modifier = Modifier.onClick(
                            rememberCallback {
                                onHamburgerTapped()
                            },
                        ),
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    )
                }

                listingType != null -> {
                    Image(
                        modifier = Modifier.onClick(
                            rememberCallback {
                                onSelectListingType?.invoke()
                            },
                        ),
                        imageVector = listingType.toIcon(),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    )
                }

                else -> {
                    Box(modifier = Modifier.size(24.dp))
                }
            }
        },
        title = {
            Column(
                modifier = Modifier
                    .padding(horizontal = Spacing.s)
                    .onClick(
                        rememberCallback {
                            onSelectListingType?.invoke()
                        },
                    ),
            ) {
                Text(
                    text = stringResource(MR.strings.navigation_search),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = listingType.toReadableName(),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        },
        actions = {
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
                Spacer(modifier = Modifier.width(Spacing.s))
            }

            Image(
                modifier = Modifier.onClick(
                    rememberCallback {
                        onSelectSortType?.invoke()
                    },
                ),
                imageVector = sortType.toIcon(),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            )
        },
    )
}