package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp

@Composable
fun PostCardFooter(
    modifier: Modifier = Modifier,
    separateUpAndDownVotes: Boolean = false,
    comments: Int? = null,
    date: String? = null,
    score: Int = 0,
    upvotes: Int = 0,
    downvotes: Int = 0,
    saved: Boolean = false,
    upVoted: Boolean = false,
    downVoted: Boolean = false,
    options: List<Option> = emptyList(),
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    var optionsExpanded by remember { mutableStateOf(false) }
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }
    val themeRepository = remember { getThemeRepository() }
    val upvoteColor by themeRepository.upvoteColor.collectAsState()
    val downvoteColor by themeRepository.downvoteColor.collectAsState()
    val defaultUpvoteColor = MaterialTheme.colorScheme.primary
    val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            val buttonModifier = Modifier.size(IconSize.m).padding(3.5.dp)
            if (comments != null) {
                Image(
                    modifier = buttonModifier.padding(1.dp)
                        .onClick(
                            onClick = rememberCallback {
                                onReply?.invoke()
                            },
                        ),
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                )
                Text(
                    modifier = Modifier.padding(end = Spacing.s),
                    text = "$comments",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (date != null) {
                Icon(
                    modifier = buttonModifier.padding(1.dp),
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = date.prettifyDate(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (options.isNotEmpty()) {
                Icon(
                    modifier = buttonModifier
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
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                modifier = buttonModifier.onClick(
                    onClick = rememberCallback {
                        onSave?.invoke()
                    },
                ),
                imageVector = if (!saved) {
                    Icons.Default.BookmarkBorder
                } else {
                    Icons.Default.Bookmark
                },
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = if (saved) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                ),
            )
            Image(
                modifier = buttonModifier
                    .onClick(
                        onClick = rememberCallback {
                            onUpVote?.invoke()
                        },
                    ),
                imageVector = Icons.Default.ArrowCircleUp,
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = if (upVoted) {
                        upvoteColor ?: defaultUpvoteColor
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ),
            )
            Text(
                text = buildAnnotatedString {
                    if (separateUpAndDownVotes) {
                        val upvoteText = upvotes.toString()
                        append(upvoteText)
                        if (upVoted) {
                            addStyle(
                                style = SpanStyle(color = upvoteColor ?: defaultUpvoteColor),
                                start = 0,
                                end = upvoteText.length
                            )
                        }
                        append(" / ")
                        val downvoteText = downvotes.toString()
                        append(downvoteText)
                        if (downVoted) {
                            addStyle(
                                style = SpanStyle(color = downvoteColor ?: defaultDownVoteColor),
                                start = upvoteText.length + 3,
                                end = upvoteText.length + 3 + downvoteText.length
                            )
                        }
                    } else {
                        val text = score.toString()
                        append(text)
                        if (upVoted) {
                            addStyle(
                                style = SpanStyle(color = upvoteColor ?: defaultUpvoteColor),
                                start = 0,
                                end = text.length
                            )
                        } else if (downVoted) {
                            addStyle(
                                style = SpanStyle(color = downvoteColor ?: defaultDownVoteColor),
                                start = 0,
                                end = length
                            )
                        }
                    }
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Image(
                modifier = buttonModifier
                    .onClick(
                        onClick = rememberCallback {
                            onDownVote?.invoke()
                        },
                    ),
                imageVector = Icons.Default.ArrowCircleDown,
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = if (downVoted) {
                        downvoteColor ?: defaultDownVoteColor
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ),
            )
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
                Text(
                    modifier = Modifier.padding(
                        horizontal = Spacing.m,
                        vertical = Spacing.s,
                    ).onClick(
                        onClick = rememberCallback {
                            optionsExpanded = false
                            onOptionSelected?.invoke(option.id)
                        },
                    ),
                    text = option.text,
                )
            }
        }
    }
}
