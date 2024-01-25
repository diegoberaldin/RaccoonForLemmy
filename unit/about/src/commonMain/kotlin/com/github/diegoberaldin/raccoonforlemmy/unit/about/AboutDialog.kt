package com.github.diegoberaldin.raccoonforlemmy.unit.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.handleUrl
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutConstants.CHANGELOG_URL
import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutConstants.REPORT_EMAIL_ADDRESS
import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutConstants.REPORT_URL
import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutConstants.WEBSITE_URL
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

class AboutDialog : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val viewModel = getScreenModel<AboutDialogMviModel>()
        viewModel.bindToLifecycle(key)

        val uriHandler = LocalUriHandler.current
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val uiState by viewModel.uiState.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }
        val detailOpener = remember { getDetailOpener() }

        AlertDialog(
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
                    text = stringResource(MR.strings.settings_about),
                    style = MaterialTheme.typography.titleLarge,
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
                            text = stringResource(MR.strings.settings_about_app_version),
                            value = uiState.version,
                        )
                    }
                    item {
                        AboutItem(
                            text = stringResource(MR.strings.settings_about_changelog),
                            vector = Icons.Default.OpenInBrowser,
                            textDecoration = TextDecoration.Underline,
                            onClick = rememberCallback {
                                navigationCoordinator.handleUrl(
                                    url = CHANGELOG_URL,
                                    openExternal = settings.openUrlsInExternalBrowser,
                                    uriHandler = uriHandler,
                                    onOpenWeb = { url ->
                                        navigationCoordinator.pushScreen(WebViewScreen(url))
                                    },
                                )
                            }
                        )
                    }
                    item {
                        Button(
                            onClick = rememberCallback {
                                navigationCoordinator.handleUrl(
                                    url = REPORT_URL,
                                    openExternal = settings.openUrlsInExternalBrowser,
                                    uriHandler = uriHandler,
                                    onOpenWeb = { url ->
                                        navigationCoordinator.pushScreen(WebViewScreen(url))
                                    },
                                )
                            },
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(MR.strings.settings_about_report_github),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Button(
                            onClick = {
                                runCatching {
                                    uriHandler.openUri("mailto:$REPORT_EMAIL_ADDRESS")
                                }
                            },
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(MR.strings.settings_about_report_email),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    item {
                        AboutItem(
                            painter = painterResource(MR.images.ic_github),
                            text = stringResource(MR.strings.settings_about_view_github),
                            textDecoration = TextDecoration.Underline,
                            onClick = rememberCallback {
                                navigationCoordinator.handleUrl(
                                    url = WEBSITE_URL,
                                    openExternal = settings.openUrlsInExternalBrowser,
                                    uriHandler = uriHandler,
                                    onOpenWeb = { url ->
                                        navigationCoordinator.pushScreen(WebViewScreen(url))
                                    },
                                )
                            },
                        )
                    }
                    item {
                        AboutItem(
                            painter = painterResource(MR.images.ic_lemmy),
                            text = stringResource(MR.strings.settings_about_view_lemmy),
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
                            painter = painterResource(MR.images.ic_matrix),
                            text = stringResource(MR.strings.settings_about_chat_matrix),
                            textDecoration = TextDecoration.Underline,
                            onClick = {
                                runCatching {
                                    uriHandler.openUri(AboutConstants.MATRIX_URL)
                                }
                            },
                        )
                    }
                }
                Button(
                    onClick = {
                        notificationCenter.send(NotificationCenterEvent.CloseDialog)
                    },
                ) {
                    Text(text = stringResource(MR.strings.button_close))
                }
            }
        }
    }

    @Composable
    fun AboutItem(
        painter: Painter? = null,
        vector: ImageVector? = null,
        text: String,
        textDecoration: TextDecoration = TextDecoration.None,
        value: String = "",
        onClick: (() -> Unit)? = null,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Spacing.xs,
                vertical = Spacing.s,
            ).onClick(
                onClick = rememberCallback {
                    onClick?.invoke()
                },
            ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val imageModifier = Modifier.size(22.dp)
            if (painter != null) {
                Image(
                    modifier = imageModifier,
                    painter = painter,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                )
            } else if (vector != null) {
                Image(
                    modifier = imageModifier,
                    imageVector = vector,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = textDecoration,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
