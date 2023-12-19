package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.shimmerEffect

@Composable
fun PostCardPlaceholder(
    postLayout: PostLayout = PostLayout.Card,
) {
    when (postLayout) {
        PostLayout.Compact -> {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    Box(
                        modifier = Modifier.size(IconSize.s)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                    Column(
                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                    ) {
                        Box(
                            modifier = Modifier.height(IconSize.s)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(CornerSize.m))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier.height(IconSize.s)
                                .fillMaxWidth(0.5f)
                                .clip(RoundedCornerShape(CornerSize.m))
                                .shimmerEffect()
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.2f)
                            .aspectRatio(1.33f)
                            .clip(RoundedCornerShape(CornerSize.s))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(CornerSize.m))
                            .shimmerEffect()
                    )
                }
                Box(
                    modifier = Modifier
                        .height(IconSize.l)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize.s))
                        .shimmerEffect()
                )
            }
        }

        PostLayout.Card -> {
            Column(
                modifier = Modifier
                    .padding(horizontal = Spacing.xs).background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                        shape = RoundedCornerShape(CornerSize.l),
                    ).padding(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    Box(
                        modifier = Modifier
                            .size(IconSize.l)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                    Column(
                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                    ) {
                        Box(
                            modifier = Modifier.height(IconSize.s)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(CornerSize.m))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier.height(IconSize.s)
                                .fillMaxWidth(0.5f)
                                .clip(RoundedCornerShape(CornerSize.m))
                                .shimmerEffect()
                        )
                    }
                }
                Box(
                    modifier = Modifier.height(IconSize.l)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize.m))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize.s))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .height(IconSize.l)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize.m))
                        .shimmerEffect()
                )
            }
        }

        PostLayout.Full -> {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    Box(
                        modifier = Modifier
                            .size(IconSize.l)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                    Column(
                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                    ) {
                        Box(
                            modifier = Modifier.height(IconSize.s)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(CornerSize.m))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier.height(IconSize.s)
                                .fillMaxWidth(0.5f)
                                .clip(RoundedCornerShape(CornerSize.m))
                                .shimmerEffect()
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .height(IconSize.l)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize.m))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize.s))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .height(IconSize.l)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize.m))
                        .shimmerEffect()
                )
            }
        }

        else -> Unit
    }
}
