package com.github.diegoberaldin.raccoonforlemmy.feature.home.postlist

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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.getAdditionalLabel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostsTopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    currentInstance: String,
    listingType: ListingType?,
    sortType: SortType?,
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
                        modifier = Modifier.onClick(
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
                    .padding(horizontal = Spacing.s)
                    .onClick(
                        onClick = rememberCallback {
                            onSelectListingType?.invoke()
                        },
                    ),
            ) {
                Text(
                    text = listingType?.toReadableName().orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(
                        MR.strings.home_instance_via,
                        currentInstance,
                    ),
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
                Spacer(modifier = Modifier.width(Spacing.s))
            }
            if (sortType != null) {
                Image(
                    modifier = Modifier.onClick(
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
