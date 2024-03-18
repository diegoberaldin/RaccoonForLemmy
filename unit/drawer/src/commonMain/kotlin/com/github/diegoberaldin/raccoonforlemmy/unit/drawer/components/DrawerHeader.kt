package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PlaceholderImage
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
internal fun DrawerHeader(
    modifier: Modifier = Modifier,
    user: UserModel? = null,
    instance: String? = null,
    autoLoadImages: Boolean = true,
    onOpenChangeInstance: (() -> Unit)? = null,
    onOpenSwitchAccount: (() -> Unit)? = null,
) {
    val avatarSize = 52.dp
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
    Row(
        modifier = modifier.padding(
            top = Spacing.m,
            start = Spacing.s,
            end = Spacing.s,
            bottom = Spacing.s,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        if (user != null) {
            // avatar
            val userAvatar = user.avatar.orEmpty()
            if (userAvatar.isNotEmpty()) {
                CustomImage(
                    modifier = Modifier.padding(Spacing.xxxs).size(avatarSize)
                        .clip(RoundedCornerShape(avatarSize / 2)),
                    url = userAvatar,
                    autoload = autoLoadImages,
                    quality = FilterQuality.Low,
                    contentScale = ContentScale.FillBounds,
                )
            } else {
                PlaceholderImage(
                    size = avatarSize,
                    title = user.name,
                )
            }

            Row {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                ) {
                    Text(
                        text = buildString {
                            if (user.displayName.isNotEmpty()) {
                                append(user.displayName)
                            } else {
                                append(user.name)
                            }
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = fullColor,
                    )
                    Text(
                        text = buildString {
                            append(user.name)
                            append("@")
                            append(user.host)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        color = ancillaryColor,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier.onClick(
                        onClick = rememberCallback {
                            onOpenSwitchAccount?.invoke()
                        },
                    ),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            }
        } else {
            val anonymousTitle = LocalXmlStrings.current.navigationDrawerAnonymous
            PlaceholderImage(
                size = avatarSize,
                title = anonymousTitle,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Text(
                    text = anonymousTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = fullColor,
                )
                Row {
                    Text(
                        text = instance.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        color = ancillaryColor,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        modifier = Modifier.onClick(
                            onClick = rememberCallback {
                                onOpenChangeInstance?.invoke()
                            },
                        ),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}