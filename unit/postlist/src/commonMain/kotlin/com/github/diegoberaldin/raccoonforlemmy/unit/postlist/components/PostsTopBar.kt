package com.github.diegoberaldin.raccoonforlemmy.unit.postlist.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostsTopBar(
    topAppBarState: TopAppBarState,
    currentInstance: String,
    listingType: ListingType?,
    sortType: SortType?,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    edgeToEdge: Boolean = true,
    onSelectListingType: (() -> Unit)? = null,
    onSelectInstance: (() -> Unit)? = null,
    onSelectSortType: (() -> Unit)? = null,
    onHamburgerTapped: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val maxTopInset = Dimensions.topBarHeight.toLocalPixel()
    var topInset by remember { mutableStateOf(maxTopInset) }
    snapshotFlow { topAppBarState.collapsedFraction }.onEach {
        topInset = maxTopInset * (1 - it)
    }.launchIn(scope)

    TopAppBar(
        windowInsets = if (edgeToEdge) {
            WindowInsets(0, topInset.roundToInt(), 0, 0)
        } else {
            TopAppBarDefaults.windowInsets
        },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            when {
                onHamburgerTapped != null -> {
                    Image(
                        modifier = Modifier
                            .onClick(
                                onClick = rememberCallback {
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
                        modifier = Modifier
                            .onClick(
                                onClick = rememberCallback {
                                    onSelectListingType?.invoke()
                                },
                            ),
                        imageVector = listingType.toIcon(),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    )
                }

                else -> {
                    Box(modifier = Modifier.size(IconSize.m))
                }
            }
        },
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.s)
                    .onClick(
                        onClick = rememberCallback {
                            onSelectListingType?.invoke()
                        },
                    ),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = listingType?.toReadableName().orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    modifier = Modifier.fillMaxWidth().then(
                        if (onSelectInstance != null) {
                            Modifier.onClick(
                                onClick = rememberCallback {
                                    onSelectInstance.invoke()
                                },
                            )
                        } else Modifier
                    ),
                    text = LocalXmlStrings.current.homeInstanceVia(currentInstance),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        actions = {
            val additionalLabel = sortType.getAdditionalLabel()
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
            if (sortType != null) {
                Image(
                    modifier = Modifier
                        .padding(horizontal = Spacing.xs)
                        .onClick(
                            onClick = rememberCallback {
                                onSelectSortType?.invoke()
                            },
                        ),
                    imageVector = sortType.toIcon(),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                )
            }
        },
    )
}
