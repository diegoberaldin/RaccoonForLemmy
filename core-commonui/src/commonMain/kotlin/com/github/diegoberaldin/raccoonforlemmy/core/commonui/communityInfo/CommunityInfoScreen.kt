package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Padding
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ScaledContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCommunityInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class CommunityInfoScreen(
    private val community: CommunityModel,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getCommunityInfoViewModel(community) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = Spacing.s,
                    start = Spacing.s,
                    end = Spacing.s,
                    bottom = Spacing.m,
                )
                .fillMaxHeight(0.9f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            BottomSheetHandle(modifier = Modifier.align(Alignment.CenterHorizontally))

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = buildString {
                                        append(uiState.community.name)
                                        if (uiState.community.host.isNotEmpty()) {
                                            append("@${uiState.community.host}")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        })
                },
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(top = Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Cake,
                                title = community.creationDate?.prettifyDate().orEmpty(),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Padding,
                                title = stringResource(MR.strings.community_info_posts),
                                value = uiState.community.posts.toString(),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Reply,
                                title = stringResource(MR.strings.community_info_comments),
                                value = uiState.community.comments.toString(),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Group,
                                title = stringResource(MR.strings.community_info_subscribers),
                                value = uiState.community.subscribers.toString(),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.CalendarViewMonth,
                                title = stringResource(MR.strings.community_info_monthly_active_users),
                                value = uiState.community.monthlyActiveUsers.toString(),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.CalendarViewWeek,
                                title = stringResource(MR.strings.community_info_weekly_active_users),
                                value = uiState.community.weeklyActiveUsers.toString(),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.CalendarViewDay,
                                title = stringResource(MR.strings.community_info_daily_active_users),
                                value = uiState.community.dailyActiveUsers.toString(),
                            )

                            Divider()
                        }
                    }
                    item {
                        ScaledContent {
                            PostCardBody(
                                modifier = Modifier.fillMaxWidth(),
                                text = uiState.community.description,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityInfoItem(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String = "",
    value: String = "",
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null)
        }
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                ) {
                    append(value)
                }
                append(" ")
                append(title)
            }
        )
    }
}
