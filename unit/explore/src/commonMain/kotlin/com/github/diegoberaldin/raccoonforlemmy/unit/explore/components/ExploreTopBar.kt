package com.github.diegoberaldin.raccoonforlemmy.unit.explore.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.getAdditionalLabel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExploreTopBar(
    topAppBarState: TopAppBarState,
    listingType: ListingType,
    sortType: SortType,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    edgeToEdge: Boolean = true,
    resultType: SearchResultType,
    otherInstance: String = "",
    onSelectListingType: (() -> Unit)? = null,
    onSelectSortType: (() -> Unit)? = null,
    onSelectResultTypeType: (() -> Unit)? = null,
    onHamburgerTapped: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val maxTopInset = Dimensions.maxTopBarInset.toLocalPixel()
    var topInset by remember { mutableStateOf(maxTopInset) }
    snapshotFlow { topAppBarState.collapsedFraction }.onEach {
        topInset = maxTopInset * (1 - it)
    }.launchIn(scope)

    TopAppBar(
        windowInsets =
            if (edgeToEdge) {
                WindowInsets(0, topInset.roundToInt(), 0, 0)
            } else {
                TopAppBarDefaults.windowInsets
            },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            when {
                otherInstance.isNotEmpty() -> {
                    Image(
                        modifier =
                            Modifier.onClick(
                                onClick = {
                                    onBack?.invoke()
                                },
                            ),
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    )
                }

                onHamburgerTapped != null -> {
                    Image(
                        modifier =
                            Modifier.onClick(
                                onClick = {
                                    onHamburgerTapped()
                                },
                            ),
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    )
                }

                else -> {
                    Image(
                        modifier =
                            Modifier.onClick(
                                onClick = {
                                    onSelectListingType?.invoke()
                                },
                            ),
                        imageVector = listingType.toIcon(),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    )
                }
            }
        },
        title = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.s)
                        .onClick(
                            onClick = {
                                if (otherInstance.isEmpty()) {
                                    onSelectListingType?.invoke()
                                }
                            },
                        ),
            ) {
                Text(
                    text =
                        buildString {
                            append(LocalXmlStrings.current.navigationSearch)
                            if (otherInstance.isNotEmpty()) {
                                append(" (")
                                append(otherInstance)
                                append(")")
                            }
                        },
                    style = MaterialTheme.typography.titleMedium,
                )
                if (otherInstance.isEmpty()) {
                    Text(
                        text = listingType.toReadableName(),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        },
        actions = {
            Image(
                modifier =
                    Modifier
                        .padding(horizontal = Spacing.xs)
                        .onClick(
                            onClick = {
                                onSelectResultTypeType?.invoke()
                            },
                        ),
                imageVector = resultType.toIcon(),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            )

            val additionalLabel = sortType.getAdditionalLabel()
            if (additionalLabel.isNotEmpty()) {
                Text(
                    text =
                        buildString {
                            append("(")
                            append(additionalLabel)
                            append(")")
                        },
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
            }

            Image(
                modifier =
                    Modifier
                        .padding(horizontal = Spacing.xs)
                        .onClick(
                            onClick = {
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
