package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.InboxTypeSheet
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di.getInboxViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesScreen
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.resources.di.getLanguageRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.di.staticString
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

object InboxScreen : Tab {
    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getInboxViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                val languageRepository = remember { getLanguageRepository() }
                val lang by languageRepository.currentLanguage.collectAsState()
                val title by remember(lang) {
                    mutableStateOf(staticString(MR.strings.navigation_inbox.desc()))
                }
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    drawerCoordinator.toggleDrawer()
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
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            val text = when (uiState.unreadOnly) {
                                true -> stringResource(MR.strings.inbox_listing_type_unread)
                                else -> stringResource(MR.strings.inbox_listing_type_all)
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
                            text = stringResource(MR.strings.inbox_not_logged_message),
                        )
                    }
                }

                true -> {
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .let {
                                if (settings.hideNavigationBarWhileScrolling) {
                                    it.nestedScroll(scrollBehavior.nestedScrollConnection)
                                } else {
                                    it
                                }
                            },
                        verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    ) {
                        SectionSelector(
                            modifier = Modifier.padding(vertical = Spacing.s),
                            titles = listOf(
                                buildString {
                                    append(stringResource(MR.strings.inbox_section_replies))
                                    if (uiState.unreadReplies > 0) {
                                        append(" (")
                                        append(uiState.unreadReplies)
                                        append(")")
                                    }
                                },
                                buildString {
                                    append(stringResource(MR.strings.inbox_section_mentions))
                                    if (uiState.unreadMentions > 0) {
                                        append(" (")
                                        append(uiState.unreadMentions)
                                        append(")")
                                    }
                                },
                                buildString {
                                    append(stringResource(MR.strings.inbox_section_messages))
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
