package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.formatToReadableValue
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FeedbackButton
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp

@Composable
fun PostCardFooter(
    modifier: Modifier = Modifier,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    comments: Int? = null,
    publishDate: String? = null,
    updateDate: String? = null,
    score: Int = 0,
    showScores: Boolean = true,
    unreadComments: Int? = null,
    upVotes: Int = 0,
    downVotes: Int = 0,
    saved: Boolean = false,
    upVoted: Boolean = false,
    downVoted: Boolean = false,
    actionButtonsActive: Boolean = true,
    markRead: Boolean = false,
    optionsMenuOpen: MutableState<Boolean> = remember { mutableStateOf(false) },
    options: List<Option> = emptyList(),
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }
    val themeRepository = remember { getThemeRepository() }
    val upVoteColor by themeRepository.upVoteColor.collectAsState()
    val downVoteColor by themeRepository.downVoteColor.collectAsState()
    val defaultUpvoteColor = MaterialTheme.colorScheme.primary
    val additionalAlphaFactor = if (markRead) 0.8f else 1f
    val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f * additionalAlphaFactor)

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            val buttonModifier = Modifier.size(IconSize.m).padding(2.5.dp)
            if (comments != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = buttonModifier.padding(1.dp)
                            .onClick(
                                onClick = rememberCallback {
                                    onReply?.invoke()
                                },
                            ),
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = ancillaryColor),
                    )
                    Text(
                        text = "$comments",
                        style = MaterialTheme.typography.labelMedium,
                        color = ancillaryColor,
                    )
                }
            }
            if (unreadComments != null) {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(CornerSize.s)
                        )
                        .padding(horizontal = Spacing.xxs),
                    text = "+$unreadComments",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondary,
                )
            }
            listOf(
                updateDate.orEmpty(),
                publishDate.orEmpty(),
            ).firstOrNull {
                it.isNotBlank()
            }?.also { publishDate ->
                Row(
                    modifier = Modifier.padding(start = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(IconSize.s),
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = ancillaryColor,
                    )
                    Text(
                        modifier = Modifier.padding(start = Spacing.xxs),
                        text = publishDate.prettifyDate(),
                        style = MaterialTheme.typography.labelMedium,
                        color = ancillaryColor,
                    )
                }
            }
            if (updateDate.orEmpty().isNotBlank()) {
                Icon(
                    modifier = buttonModifier,
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = ancillaryColor,
                )
            }
            if (options.isNotEmpty()) {
                Icon(
                    modifier = Modifier.size(IconSize.m)
                        .padding(Spacing.xs)
                        .onGloballyPositioned {
                            optionsOffset = it.positionInParent()
                        }
                        .onClick(
                            onClick = rememberCallback {
                                optionsMenuOpen.value = true
                            },
                        ),
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = ancillaryColor,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (actionButtonsActive) {
                FeedbackButton(
                    modifier = buttonModifier,
                    imageVector = if (!saved) {
                        Icons.Default.BookmarkBorder
                    } else {
                        Icons.Default.Bookmark
                    },
                    tintColor = if (saved) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        ancillaryColor
                    },
                    onClick = rememberCallback {
                        onSave?.invoke()
                    },
                )
            }
            FeedbackButton(
                modifier = buttonModifier,
                imageVector = if (actionButtonsActive) {
                    Icons.Default.ArrowCircleUp
                } else {
                    Icons.Default.ArrowUpward
                },
                tintColor = if (upVoted) {
                    upVoteColor ?: defaultUpvoteColor
                } else {
                    ancillaryColor
                },
                onClick = rememberCallback {
                    onUpVote?.invoke()
                },
            )
            if (showScores) {
                Text(
                    text = formatToReadableValue(
                        voteFormat = voteFormat,
                        score = score,
                        upVotes = upVotes,
                        downVotes = downVotes,
                        upVoteColor = upVoteColor ?: defaultUpvoteColor,
                        downVoteColor = downVoteColor ?: defaultDownVoteColor,
                        upVoted = upVoted,
                        downVoted = downVoted,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = ancillaryColor,
                )
            }
            FeedbackButton(
                modifier = buttonModifier,
                imageVector = if (actionButtonsActive) {
                    Icons.Default.ArrowCircleDown
                } else {
                    Icons.Default.ArrowDownward
                },
                tintColor = if (downVoted) {
                    downVoteColor ?: defaultDownVoteColor
                } else {
                    ancillaryColor
                },
                onClick = rememberCallback {
                    onDownVote?.invoke()
                },
            )
        }

        CustomDropDown(
            expanded = optionsMenuOpen.value,
            onDismiss = {
                optionsMenuOpen.value = false
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
                        optionsMenuOpen.value = false
                        onOptionSelected?.invoke(option.id)
                    },
                )
            }
        }
    }
}


