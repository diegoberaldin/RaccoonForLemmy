package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.InboxTypeSheet
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di.getInboxViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesScreen
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.resources.di.getLanguageRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.di.staticString
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.desc

class InboxScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getInboxViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


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
                    title = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            val text = when (uiState.unreadOnly) {
                                true -> stringResource(MR.strings.inbox_listing_type_unread)
                                else -> stringResource(MR.strings.inbox_listing_type_all)
                            }
                            Text(
                                modifier = Modifier.onClick {
                                    bottomSheetNavigator.show(
                                        InboxTypeSheet(
                                            onUnreadSelected = {
                                                model.reduce(
                                                    InboxMviModel.Intent.ChangeUnreadOnly(
                                                        unread = it,
                                                    ),
                                                )
                                            },
                                            onHide = {
                                                bottomSheetNavigator.hide()
                                            },
                                        ),
                                    )
                                },
                                text = text,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    },
                    actions = {
                        Image(
                            modifier = Modifier.onClick {
                                model.reduce(InboxMviModel.Intent.ReadAll)
                            },
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                when (uiState.section) {
                    InboxSection.REPLIES -> InboxRepliesScreen(
                        parentModel = model,
                    ).Content()

                    InboxSection.MENTIONS -> InboxMentionsScreen(
                        parentModel = model,
                    ).Content()

                    InboxSection.MESSAGES -> Unit
                }
            }
        }
    }
}
