package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.InboxTypeSheet
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.subscribe
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
import kotlinx.coroutines.launch

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
        val notificationCenter = remember { getNotificationCenter() }
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scope = rememberCoroutineScope()
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()

        LaunchedEffect(notificationCenter) {
            notificationCenter.subscribe<NotificationCenterEvent.ChangeInboxType>().onEach { evt ->
                model.reduce(
                    InboxMviModel.Intent.ChangeUnreadOnly(evt.unreadOnly)
                )
            }.launchIn(this)
        }

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
                        Column(modifier = Modifier.padding(horizontal = Spacing.s)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            val text = when (uiState.unreadOnly) {
                                true -> stringResource(MR.strings.inbox_listing_type_unread)
                                else -> stringResource(MR.strings.inbox_listing_type_all)
                            }
                            Text(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        val sheet = InboxTypeSheet()
                                        navigationCoordinator.showBottomSheet(sheet)
                                    },
                                ),
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
                                stringResource(MR.strings.inbox_section_replies),
                                stringResource(MR.strings.inbox_section_mentions),
                                stringResource(MR.strings.inbox_section_messages),
                            ),
                            currentSection = when (uiState.section) {
                                InboxSection.MENTIONS -> 1
                                InboxSection.MESSAGES -> 2
                                else -> 0
                            },
                            onSectionSelected = {
                                val section = when (it) {
                                    1 -> InboxSection.MENTIONS
                                    2 -> InboxSection.MESSAGES
                                    else -> InboxSection.REPLIES
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
                                        InboxSection.REPLIES -> 0
                                        InboxSection.MENTIONS -> 1
                                        InboxSection.MESSAGES -> 2
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
