package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di.getInboxChatViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

class InboxChatScreen(
    private val otherUserId: Int,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getInboxChatViewModel(otherUserId) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val iconSize = 23.dp
                            val avatar = uiState.otherUserAvatar.orEmpty()
                            if (avatar.isNotEmpty()) {
                                val painterResource = asyncPainterResource(data = avatar)
                                KamelImage(
                                    modifier = Modifier.padding(Spacing.xxxs).size(iconSize)
                                        .clip(RoundedCornerShape(iconSize / 2)),
                                    resource = painterResource,
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                )
                            }
                            Text(
                                modifier = Modifier.padding(horizontal = Spacing.s),
                                text = uiState.otherUserName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick {
                                navigator?.pop()
                            },
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        )
                    },
                )
            },
        ) { padding ->
            if (uiState.currentUserId != 0) {
                Box(
                    modifier = Modifier.padding(padding)
                ) {
                    Column {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                            reverseLayout = true,
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(Spacing.s))
                            }
                            items(uiState.messages) { message ->
                                val themeRepository = remember { getThemeRepository() }
                                val fontScale by themeRepository.contentFontScale.collectAsState()
                                CompositionLocalProvider(
                                    LocalDensity provides Density(
                                        density = LocalDensity.current.density,
                                        fontScale = fontScale,
                                    ),
                                ) {
                                    val isMyMessage = message.creator?.id == uiState.currentUserId
                                    val content = message.content.orEmpty()
                                    val date = message.publishDate.orEmpty()
                                    MessageCard(
                                        isMyMessage = isMyMessage,
                                        content = content,
                                        date = date,
                                    )
                                }
                            }
                            item {
                                if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                    model.reduce(InboxChatMviModel.Intent.LoadNextPage)
                                }
                                if (uiState.loading && !uiState.refreshing) {
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
                            item {
                                Spacer(modifier = Modifier.height(Spacing.xxxl))
                            }
                        }

                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val focusManager = LocalFocusManager.current
                            Box(
                                modifier = Modifier.weight(1f).background(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(CornerSize.l)
                                ).padding(Spacing.s)
                            ) {
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 20.dp, max = 200.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                                    value = uiState.newMessageContent,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Ascii,
                                        autoCorrect = false,
                                        imeAction = ImeAction.Send,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            model.reduce(InboxChatMviModel.Intent.SubmitNewMessage)
                                            focusManager.clearFocus()
                                        }
                                    ),
                                    onValueChange = { value ->
                                        model.reduce(
                                            InboxChatMviModel.Intent.SetNewMessageContent(
                                                value
                                            )
                                        )
                                    },
                                )
                            }
                            IconButton(
                                onClick = {
                                    model.reduce(InboxChatMviModel.Intent.SubmitNewMessage)
                                    focusManager.clearFocus()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
