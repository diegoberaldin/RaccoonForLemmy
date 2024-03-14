package com.github.diegoberaldin.raccoonforlemmy.unit.drafts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Padding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardTitle
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftType
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.toIso8601Timestamp
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp

@Composable
fun DraftCard(
    draft: DraftModel,
    postLayout: PostLayout,
    modifier: Modifier = Modifier,
    options: List<Option> = emptyList(),
    onOpen: () -> Unit,
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    Box(
        modifier = modifier.then(
            if (postLayout == PostLayout.Card) {
                Modifier
                    .shadow(elevation = 5.dp, shape = RoundedCornerShape(CornerSize.l))
                    .clip(RoundedCornerShape(CornerSize.l))
                    .padding(horizontal = Spacing.xs)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                        shape = RoundedCornerShape(CornerSize.l),
                    )
                    .padding(Spacing.xs)
            } else {
                Modifier.background(MaterialTheme.colorScheme.background)
            }
        ).onClick(onClick = onOpen),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            draft.reference?.also { reference ->
                CustomizedContent(ContentFontClass.AncillaryText) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        val imageVector = when (draft.type) {
                            DraftType.Comment -> Icons.AutoMirrored.Filled.Reply
                            DraftType.Post -> Icons.Default.Padding
                        }
                        Icon(
                            imageVector = imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(IconSize.s),
                        )
                        Text(
                            text = reference,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                        )
                    }
                }
            }

            draft.title?.also { title ->
                CustomizedContent(ContentFontClass.Title) {
                    PostCardTitle(
                        modifier = Modifier.padding(
                            horizontal = Spacing.xs,
                        ),
                        text = title,
                        onClick = onOpen,
                    )
                }
            }

            CustomizedContent(ContentFontClass.Body) {
                PostCardBody(
                    modifier = Modifier.padding(
                        horizontal = Spacing.xs,
                    ),
                    text = draft.body,
                    maxLines = 40,
                    onClick = onOpen
                )
            }
            DraftFooter(
                date = draft.date?.toIso8601Timestamp(),
                options = options,
                onOptionSelected = onOptionSelected,
            )
        }
    }
}

@Composable
private fun DraftFooter(
    date: String? = null,
    options: List<Option> = emptyList(),
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    val buttonModifier = Modifier.size(IconSize.m).padding(3.dp)
    var optionsExpanded by remember { mutableStateOf(false) }
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = buttonModifier,
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = ancillaryColor,
                )
                Text(
                    text = date?.prettifyDate() ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    color = ancillaryColor,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (options.isNotEmpty()) {
                Icon(
                    modifier = Modifier.size(IconSize.m)
                        .padding(Spacing.xs)
                        .padding(top = Spacing.xxs)
                        .onGloballyPositioned {
                            optionsOffset = it.positionInParent()
                        }
                        .onClick(
                            onClick = rememberCallback {
                                optionsExpanded = true
                            },
                        ),
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = ancillaryColor,
                )
            }
        }
        CustomDropDown(
            expanded = optionsExpanded,
            onDismiss = {
                optionsExpanded = false
            },
            offset = DpOffset(
                x = optionsOffset.x.toLocalDp(),
                y = optionsOffset.y.toLocalDp(),
            ),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option.text)
                    },
                    onClick = rememberCallback {
                        optionsExpanded = false
                        onOptionSelected?.invoke(option.id)
                    },
                )
            }
        }
    }
}
