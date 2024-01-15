package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.formatToReadableValue
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FeedbackButton
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
fun InboxReplySubtitle(
    modifier: Modifier = Modifier,
    autoLoadImages: Boolean = true,
    creator: UserModel? = null,
    community: CommunityModel? = null,
    iconSize: Dp = IconSize.s,
    date: String? = null,
    score: Int = 0,
    upVotes: Int = 0,
    downVotes: Int = 0,
    options: List<Option> = emptyList(),
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    upVoted: Boolean = false,
    downVoted: Boolean = false,
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    val buttonModifier = Modifier.size(IconSize.m).padding(3.dp)
    val themeRepository = remember { getThemeRepository() }
    val upVoteColor by themeRepository.upVoteColor.collectAsState()
    val downVoteColor by themeRepository.downVoteColor.collectAsState()
    val defaultUpvoteColor = MaterialTheme.colorScheme.primary
    val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
    var optionsExpanded by remember { mutableStateOf(false) }
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)

    Column(
        modifier = modifier,
    ) {
        val communityName = community?.name.orEmpty()
        val communityIcon = community?.icon.orEmpty()
        val communityHost = community?.host.orEmpty()
        val creatorName = creator?.name.orEmpty()
        val creatorAvatar = creator?.avatar.orEmpty()
        val creatorHost = creator?.host.orEmpty()
        if (communityName.isNotEmpty() || creatorName.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                if (creatorName.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .onClick(
                                onClick = rememberCallback {
                                    if (creator != null) {
                                        onOpenCreator?.invoke(creator)
                                    }
                                },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        if (creatorAvatar.isNotEmpty() && autoLoadImages) {
                            CustomImage(
                                modifier = Modifier
                                    .padding(Spacing.xxxs)
                                    .size(iconSize)
                                    .clip(RoundedCornerShape(iconSize / 2)),
                                url = creatorAvatar,
                                quality = FilterQuality.Low,
                                contentScale = ContentScale.FillBounds,
                            )
                        }
                        Text(
                            modifier = Modifier.padding(vertical = Spacing.xs),
                            text = buildString {
                                append(creatorName)
                                if (creatorHost.isNotEmpty() && communityHost != creatorHost) {
                                    append("@$creatorHost")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = ancillaryColor,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    }
                }
                if (communityName.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .onClick(
                                onClick = rememberCallback {
                                    if (community != null) {
                                        onOpenCommunity?.invoke(community)
                                    }
                                },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        if (communityIcon.isNotEmpty() && autoLoadImages) {
                            CustomImage(
                                modifier = Modifier
                                    .padding(Spacing.xxxs)
                                    .size(iconSize)
                                    .clip(RoundedCornerShape(iconSize / 2)),
                                url = communityIcon,
                                quality = FilterQuality.Low,
                                contentScale = ContentScale.FillBounds,
                            )
                        }
                        Text(
                            modifier = Modifier.padding(vertical = Spacing.xs),
                            text = buildString {
                                append(communityName)
                                if (communityHost.isNotEmpty()) {
                                    append("@$communityHost")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = ancillaryColor,
                        )
                    }
                }
            }
        }

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
                if (options.isNotEmpty()) {
                    Icon(
                        modifier = Modifier.size(IconSize.m)
                            .padding(Spacing.xs)
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
                        tint = ancillaryColor
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                FeedbackButton(
                    modifier = buttonModifier.onClick(),
                    imageVector = Icons.Default.ArrowCircleUp,
                    tintColor = if (upVoted) {
                        upVoteColor ?: defaultUpvoteColor
                    } else {
                        ancillaryColor
                    },
                    onClick = rememberCallback {
                        onUpVote?.invoke()
                    },
                )
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
                    style = MaterialTheme.typography.labelLarge,
                    color = ancillaryColor,
                )
                FeedbackButton(
                    modifier = buttonModifier.onClick(),
                    imageVector = Icons.Default.ArrowCircleDown,
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
}
