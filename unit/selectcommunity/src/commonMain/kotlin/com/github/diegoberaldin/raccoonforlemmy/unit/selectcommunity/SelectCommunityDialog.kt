package com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SearchField
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItemPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback

class SelectCommunityDialog : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<SelectCommunityMviModel>()
        val uiState by model.uiState.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }

        BasicAlertDialog(
            onDismissRequest =
                rememberCallback {
                    model.reduce(SelectCommunityMviModel.Intent.SetSearch(""))
                    notificationCenter.send(NotificationCenterEvent.CloseDialog)
                },
        ) {
            Column(
                modifier =
                    Modifier
                        .background(color = MaterialTheme.colorScheme.surface)
                        .padding(vertical = Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = LocalStrings.current.dialogTitleSelectCommunity,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(Spacing.s))

                SearchField(
                    modifier = Modifier.fillMaxWidth(),
                    hint = LocalStrings.current.exploreSearchPlaceholder,
                    value = uiState.searchText,
                    onValueChange = { value ->
                        model.reduce(SelectCommunityMviModel.Intent.SetSearch(value))
                    },
                    onClear = {
                        model.reduce(SelectCommunityMviModel.Intent.SetSearch(""))
                    },
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 500.dp, max = 500.dp)
                            .padding(horizontal = Spacing.xs),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                    ) {
                        if (uiState.communities.isEmpty() && uiState.initial) {
                            items(5) {
                                CommunityItemPlaceholder()
                            }
                        }
                        items(uiState.communities, { it.id }) { community ->
                            CommunityItem(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .onClick(
                                            onClick = {
                                                notificationCenter.send(
                                                    NotificationCenterEvent.SelectCommunity(community),
                                                )
                                                notificationCenter.send(NotificationCenterEvent.CloseDialog)
                                            },
                                        ),
                                autoLoadImages = uiState.autoLoadImages,
                                preferNicknames = uiState.preferNicknames,
                                community = community,
                            )
                        }

                        item {
                            if (!uiState.initial && !uiState.loading && uiState.canFetchMore) {
                                model.reduce(SelectCommunityMviModel.Intent.LoadNextPage)
                            }
                            if (!uiState.initial && uiState.loading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.xs),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(25.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        model.reduce(SelectCommunityMviModel.Intent.SetSearch(""))
                        notificationCenter.send(NotificationCenterEvent.CloseDialog)
                    },
                ) {
                    Text(text = LocalStrings.current.buttonClose)
                }
            }
        }
    }
}
