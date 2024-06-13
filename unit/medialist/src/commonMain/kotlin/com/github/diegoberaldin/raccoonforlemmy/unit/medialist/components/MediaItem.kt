package com.github.diegoberaldin.raccoonforlemmy.unit.medialist.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardVideo
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.looksLikeAVideo
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MediaModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl

@Composable
internal fun MediaItem(
    media: MediaModel,
    modifier: Modifier = Modifier,
    postLayout: PostLayout = PostLayout.Card,
    fullHeightImage: Boolean = true,
    fullWidthImage: Boolean = false,
    options: List<Option> = emptyList(),
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    val mediaUrl = media.alias
    val backgroundColor =
        when (postLayout) {
            PostLayout.Card -> MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
            else -> MaterialTheme.colorScheme.background
        }

    Column(
        modifier =
            modifier.background(backgroundColor),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        if (mediaUrl.looksLikeAVideo) {
            PostCardVideo(
                modifier =
                    Modifier
                        .padding(
                            vertical = Spacing.xxs,
                            horizontal = if (fullWidthImage) 0.dp else Spacing.s,
                        ),
                url = mediaUrl,
                backgroundColor = backgroundColor,
            )
        } else {
            PostCardImage(
                modifier =
                    Modifier
                        .weight(0.25f)
                        .then(
                            if (fullHeightImage) {
                                Modifier
                            } else {
                                Modifier.aspectRatio(1f)
                            },
                        )
                        .padding(vertical = Spacing.xs)
                        .clip(RoundedCornerShape(CornerSize.s)),
                minHeight = Dp.Unspecified,
                maxHeight = Dp.Unspecified,
                imageUrl = mediaUrl,
                loadButtonContent = @Composable {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                },
            )
        }

        MediaFooter(
            modifier =
                Modifier.padding(
                    vertical = Spacing.xs,
                    horizontal = Spacing.s,
                ),
            date = media.date,
            options = options,
            onOptionSelected = onOptionSelected,
        )
    }
}

@Composable
private fun MediaFooter(
    modifier: Modifier = Modifier,
    date: String? = null,
    options: List<Option> = emptyList(),
    onOpen: (() -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    val buttonModifier = Modifier.size(IconSize.l).padding(3.dp)
    var optionsExpanded by remember { mutableStateOf(false) }
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha)

    Box(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(IconSize.m).padding(1.5.dp),
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = ancillaryColor,
                )
                Text(
                    modifier = Modifier.padding(start = Spacing.xxs),
                    text = date?.prettifyDate() ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = ancillaryColor,
                )
            }
            if (options.isNotEmpty()) {
                Icon(
                    modifier =
                        Modifier.size(IconSize.m)
                            .padding(Spacing.xs)
                            .padding(top = Spacing.xxs)
                            .onGloballyPositioned {
                                optionsOffset = it.positionInParent()
                            }
                            .onClick(
                                onClick = {
                                    optionsExpanded = true
                                },
                            ),
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = ancillaryColor,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (onOpen != null) {
                Image(
                    modifier =
                        buttonModifier
                            .onClick(
                                onClick = {
                                    onOpen.invoke()
                                },
                            ),
                    imageVector = Icons.AutoMirrored.Default.OpenInNew,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                )
            }
        }
        CustomDropDown(
            expanded = optionsExpanded,
            onDismiss = {
                optionsExpanded = false
            },
            offset =
                DpOffset(
                    x = optionsOffset.x.toLocalDp(),
                    y = optionsOffset.y.toLocalDp(),
                ),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option.text)
                    },
                    onClick = {
                        optionsExpanded = false
                        onOptionSelected?.invoke(option.id)
                    },
                )
            }
        }
    }
}
