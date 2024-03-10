package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.InboxTypeSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.unit.mentions.InboxMentionsScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.messages.InboxMessagesScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.replies.InboxRepliesScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

object InboxScreen : Tab {
    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<InboxMviModel>()
        val uiState by model.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val scope = rememberCoroutineScope()

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xxs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    scope.launch {
                                        drawerCoordinator.toggleDrawer()
                                    }
                                },
                            ),
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    title = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.s)
                                .onClick(
                                    onClick = rememberCallback {
                                        val sheet = InboxTypeSheet()
                                        navigationCoordinator.showBottomSheet(sheet)
                                    },
                                )
                        ) {
                            Text(
                                text = LocalXmlStrings.current.navigationInbox,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            val text = when (uiState.unreadOnly) {
                                true -> LocalXmlStrings.current.inboxListingTypeUnread
                                else -> LocalXmlStrings.current.inboxListingTypeAll
                            }
                            Text(
                                text = text,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    },
                    actions = {
                        if (uiState.isLogged == true) {
                            Image(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        model.reduce(InboxMviModel.Intent.ReadAll)
                                    },
                                ),
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            when (uiState.isLogged) {
                false -> {
                    Column(
                        modifier = Modifier.padding(paddingValues).padding(horizontal = Spacing.m)
                    ) {
                        Text(
                            text = LocalXmlStrings.current.inboxNotLoggedMessage,
                        )
                    }
                }

                true -> {
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .then(
                                if (settings.hideNavigationBarWhileScrolling) {
                                    Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                                } else {
                                    Modifier
                                }
                            ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    ) {
                        SectionSelector(
                            modifier = Modifier.padding(vertical = Spacing.xs),
                            titles = listOf(
                                buildString {
                                    append(LocalXmlStrings.current.inboxSectionReplies)
                                    if (uiState.unreadReplies > 0) {
                                        append(" (")
                                        append(uiState.unreadReplies)
                                        append(")")
                                    }
                                },
                                buildString {
                                    append(LocalXmlStrings.current.inboxSectionMentions)
                                    if (uiState.unreadMentions > 0) {
                                        append(" (")
                                        append(uiState.unreadMentions)
                                        append(")")
                                    }
                                },
                                buildString {
                                    append(LocalXmlStrings.current.inboxSectionMessages)
                                    if (uiState.unreadMessages > 0) {
                                        append(" (")
                                        append(uiState.unreadMessages)
                                        append(")")
                                    }
                                },
                            ),
                            currentSection = when (uiState.section) {
                                InboxSection.Mentions -> 1
                                InboxSection.Messages -> 2
                                else -> 0
                            },
                            onSectionSelected = {
                                val section = when (it) {
                                    1 -> InboxSection.Mentions
                                    2 -> InboxSection.Messages
                                    else -> InboxSection.Replies
                                }
                                model.reduce(InboxMviModel.Intent.ChangeSection(section))
                            },
                        )
                        val screens = remember {
                            listOf(
                                InboxRepliesScreen(),
                                InboxMentionsScreen(),
                                InboxMessagesScreen(),
                            )
                        }
                        TabNavigator(screens.first()) {
                            CurrentScreen()
                            val navigator = LocalTabNavigator.current
                            LaunchedEffect(model) {
                                model.uiState.map { it.section }.onEach { section ->
                                    val index = when (section) {
                                        InboxSection.Replies -> 0
                                        InboxSection.Mentions -> 1
                                        InboxSection.Messages -> 2
                                    }
                                    navigator.current = screens[index]
                                }.launchIn(this)
                            }
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}
