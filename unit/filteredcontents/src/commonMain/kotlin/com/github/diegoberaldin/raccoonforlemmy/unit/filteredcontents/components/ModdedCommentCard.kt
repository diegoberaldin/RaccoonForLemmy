package com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
fun ModdedCommentCard(
    comment: CommentModel,
    postLayout: PostLayout,
    modifier: Modifier = Modifier,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
    options: List<Option> = emptyList(),
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onOpenUser: ((UserModel, String) -> Unit)? = null,
    onOpen: (() -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.then(
            if (postLayout == PostLayout.Card) {
                Modifier.shadow(elevation = 5.dp, shape = RoundedCornerShape(CornerSize.l))
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
        ).clickable(onClick = { onOpen?.invoke() }),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            CommentCard(
                comment = comment,
                voteFormat = voteFormat,
                autoLoadImages = autoLoadImages,
                preferNicknames = preferNicknames,
                showExpandedIndicator = false,
                hideIndent = true,
                onClick = onOpen,
                onOpenCreator = onOpenUser,
                onUpVote = onUpVote,
                onDownVote = onDownVote,
                onSave = onSave,
                onReply = onReply,
            )

            ModdedCommentFooter(
                modifier = Modifier.padding(horizontal = Spacing.xs),
                communityName = comment.community?.name,
                postTitle = comment.postTitle,
                options = options,
                onOptionSelected = onOptionSelected,
            )
        }
    }
}

@Composable
private fun ModdedCommentFooter(
    modifier: Modifier = Modifier,
    communityName: String? = null,
    postTitle: String? = null,
    options: List<Option> = emptyList(),
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    var optionsExpanded by remember { mutableStateOf(false) }
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha)

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = Spacing.xs,
                    end = Spacing.xs,
                    bottom = Spacing.xs,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                postTitle?.also { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = fullColor
                    )
                }
                communityName?.also { community ->
                    Text(
                        text = community,
                        style = MaterialTheme.typography.labelSmall,
                        color = ancillaryColor
                    )
                }
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
