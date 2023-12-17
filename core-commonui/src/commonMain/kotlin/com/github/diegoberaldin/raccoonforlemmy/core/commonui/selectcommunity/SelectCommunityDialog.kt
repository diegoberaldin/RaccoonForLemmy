package com.github.diegoberaldin.raccoonforlemmy.core.commonui.selectcommunity

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getSelectCommunityViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class SelectCommunityDialog : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getSelectCommunityViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }

        AlertDialog(
            onDismissRequest = rememberCallback {
                model.reduce(SelectCommunityMviModel.Intent.SetSearch(""))
                notificationCenter.send(NotificationCenterEvent.CloseDialog)
            },
        ) {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(vertical = Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(MR.strings.dialog_title_select_community),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(Spacing.s))

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    label = {
                        Text(text = stringResource(MR.strings.explore_search_placeholder))
                    },
                    singleLine = true,
                    value = uiState.searchText,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                    ),
                    onValueChange = { value ->
                        model.reduce(SelectCommunityMviModel.Intent.SetSearch(value))
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    if (uiState.searchText.isNotEmpty()) {
                                        model.reduce(SelectCommunityMviModel.Intent.SetSearch(""))
                                    }
                                },
                            ),
                            imageVector = if (uiState.searchText.isEmpty()) Icons.Default.Search else Icons.Default.Clear,
                            contentDescription = null,
                        )
                    },
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 500.dp, max = 500.dp)
                        .padding(horizontal = Spacing.m)
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
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .onClick(
                                        onClick = rememberCallback {
                                            notificationCenter.send(
                                                NotificationCenterEvent.SelectCommunity(community)
                                            )
                                            notificationCenter.send(NotificationCenterEvent.CloseDialog)
                                        },
                                    ),
                                autoLoadImages = uiState.autoLoadImages,
                                community = community,
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        model.reduce(SelectCommunityMviModel.Intent.SetSearch(""))
                        notificationCenter.send(NotificationCenterEvent.CloseDialog)
                    },
                ) {
                    Text(text = stringResource(MR.strings.button_close))
                }
            }
        }
    }
}
