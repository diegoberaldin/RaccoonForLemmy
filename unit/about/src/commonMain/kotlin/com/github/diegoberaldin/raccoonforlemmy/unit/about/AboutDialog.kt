package com.github.diegoberaldin.raccoonforlemmy.unit.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.handleUrl
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.resources.di.getCoreResources
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.getCustomTabsHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.toUrlOpeningMode
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.unit.about.components.AboutItem
import com.github.diegoberaldin.raccoonforlemmy.unit.licences.LicencesScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen

class AboutDialog : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<AboutDialogMviModel>()
        val uriHandler = LocalUriHandler.current
        val customTabsHelper = remember { getCustomTabsHelper() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val uiState by viewModel.uiState.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }
        val detailOpener = remember { getDetailOpener() }
        val coreResources = remember { getCoreResources() }

        BasicAlertDialog(
            onDismissRequest = {
                notificationCenter.send(NotificationCenterEvent.CloseDialog)
            },
        ) {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(vertical = Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = LocalXmlStrings.current.settingsAbout,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(Spacing.s))
                LazyColumn(
                    modifier = Modifier
                        .padding(vertical = Spacing.s, horizontal = Spacing.m)
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    item {
                        AboutItem(
                            text = LocalXmlStrings.current.settingsAboutAppVersion,
                            value = uiState.version,
                        )
                    }
                    item {
                        AboutItem(
                            text = LocalXmlStrings.current.settingsAboutChangelog,
                            vector = Icons.Default.OpenInBrowser,
                            textDecoration = TextDecoration.Underline,
                            onClick = {
                                navigationCoordinator.handleUrl(
                                    url = AboutConstants.CHANGELOG_URL,
                                    openingMode = settings.urlOpeningMode.toUrlOpeningMode(),
                                    uriHandler = uriHandler,
                                    customTabsHelper = customTabsHelper,
                                    onOpenWeb = { url ->
                                        navigationCoordinator.pushScreen(WebViewScreen(url))
                                    },
                                )
                            }
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                navigationCoordinator.handleUrl(
                                    url = AboutConstants.REPORT_URL,
                                    openingMode = settings.urlOpeningMode.toUrlOpeningMode(),
                                    uriHandler = uriHandler,
                                    customTabsHelper = customTabsHelper,
                                    onOpenWeb = { url ->
                                        navigationCoordinator.pushScreen(WebViewScreen(url))
                                    },
                                )
                            },
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = LocalXmlStrings.current.settingsAboutReportGithub,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Button(
                            onClick = {
                                runCatching {
                                    uriHandler.openUri("mailto:${AboutConstants.REPORT_EMAIL_ADDRESS}")
                                }
                            },
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = LocalXmlStrings.current.settingsAboutReportEmail,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    item {
                        AboutItem(
                            painter = coreResources.github,
                            text = LocalXmlStrings.current.settingsAboutViewGithub,
                            textDecoration = TextDecoration.Underline,
                            onClick = {
                                navigationCoordinator.handleUrl(
                                    url = AboutConstants.WEBSITE_URL,
                                    openingMode = settings.urlOpeningMode.toUrlOpeningMode(),
                                    uriHandler = uriHandler,
                                    customTabsHelper = customTabsHelper,
                                    onOpenWeb = { url ->
                                        navigationCoordinator.pushScreen(WebViewScreen(url))
                                    },
                                )
                            },
                        )
                    }
                    item {
                        AboutItem(
                            vector = Icons.Default.Shop,
                            text = LocalXmlStrings.current.settingsAboutViewGooglePlay,
                            textDecoration = TextDecoration.Underline,
                            onClick = {
                                navigationCoordinator.handleUrl(
                                    url = AboutConstants.GOOGLE_PLAY_URL,
                                    openingMode = settings.urlOpeningMode.toUrlOpeningMode(),
                                    uriHandler = uriHandler,
                                    customTabsHelper = customTabsHelper,
                                    onOpenWeb = { url ->
                                        navigationCoordinator.pushScreen(WebViewScreen(url))
                                    },
                                )
                            },
                        )
                    }
                    item {
                        AboutItem(
                            painter = coreResources.lemmy,
                            text = LocalXmlStrings.current.settingsAboutViewLemmy,
                            textDecoration = TextDecoration.Underline,
                            onClick = {
                                detailOpener.openCommunityDetail(
                                    community = CommunityModel(name = AboutConstants.LEMMY_COMMUNITY_NAME),
                                    otherInstance = AboutConstants.LEMMY_COMMUNITY_INSTANCE
                                )
                            },
                        )
                    }

                    item {
                        AboutItem(
                            text = LocalXmlStrings.current.settingsAboutLicences,
                            vector = Icons.Default.Gavel,
                            textDecoration = TextDecoration.Underline,
                            onClick = {
                                navigationCoordinator.pushScreen(LicencesScreen())
                            },
                        )
                    }
                }
                Button(
                    onClick = {
                        notificationCenter.send(NotificationCenterEvent.CloseDialog)
                    },
                ) {
                    Text(text = LocalXmlStrings.current.buttonClose)
                }
            }
        }
    }
}

