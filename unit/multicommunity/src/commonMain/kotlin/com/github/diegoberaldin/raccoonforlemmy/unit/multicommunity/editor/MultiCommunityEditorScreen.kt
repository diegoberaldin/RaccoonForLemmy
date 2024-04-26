package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toReadableMessage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.parameter.parametersOf

class MultiCommunityEditorScreen(
    private val communityId: Long? = null,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<MultiCommunityEditorMviModel> { parametersOf(communityId) }
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }

        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    MultiCommunityEditorMviModel.Effect.Close -> {
                        navigationCoordinator.popScreen()
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = LocalXmlStrings.current.multiCommunityEditorTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    navigationCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    actions = {
                        IconButton(
                            modifier = Modifier.padding(horizontal = Spacing.xs),
                            onClick = {
                                model.reduce(MultiCommunityEditorMviModel.Intent.Submit)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            val focusManager = LocalFocusManager.current
            val keyboardScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        focusManager.clearFocus()
                        return Offset.Zero
                    }
                }
            }
            Column(
                modifier = Modifier.padding(paddingValues).nestedScroll(keyboardScrollConnection),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    maxLines = 1,
                    label = {
                        Text(text = LocalXmlStrings.current.multiCommunityEditorName)
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = uiState.name,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrect = false,
                        imeAction = ImeAction.Next,
                    ),
                    onValueChange = { value ->
                        model.reduce(MultiCommunityEditorMviModel.Intent.SetName(value))
                    },
                    isError = uiState.nameError != null,
                    supportingText = {
                        val error = uiState.nameError
                        if (error != null) {
                            Text(
                                text = error.toReadableMessage(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )

                Spacer(modifier = Modifier.height(Spacing.s))
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.m)
                ) {
                    Text(text = LocalXmlStrings.current.multiCommunityEditorIcon)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    ) {
                        val iconSize = 40.dp
                        if (uiState.autoLoadImages) {
                            itemsIndexed(uiState.availableIcons) { idx, url ->
                                val selected = url == uiState.icon
                                CustomImage(
                                    modifier = Modifier
                                        .size(iconSize)
                                        .clip(RoundedCornerShape(iconSize / 2))
                                        .let {
                                            if (selected) {
                                                it.border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape,
                                                ).padding(1.dp).border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    shape = CircleShape,
                                                ).padding(1.dp).border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape,
                                                )
                                            } else {
                                                it
                                            }
                                        }.onClick(
                                            onClick = rememberCallback(model) {
                                                model.reduce(
                                                    MultiCommunityEditorMviModel.Intent.SelectImage(
                                                        idx,
                                                    )
                                                )
                                            },
                                        ),
                                    url = url,
                                    contentScale = ContentScale.FillBounds,
                                )
                            }
                        }
                        item {
                            val selected = uiState.icon == null
                            Box(
                                modifier = Modifier
                                    .padding(Spacing.xxxs)
                                    .size(iconSize)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(iconSize / 2),
                                    ).let {
                                        if (selected) {
                                            it.border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape,
                                            ).padding(1.dp).border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                shape = CircleShape,
                                            ).padding(1.dp).border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape,
                                            )
                                        } else {
                                            it
                                        }
                                    }.onClick(
                                        onClick = rememberCallback {
                                            model.reduce(
                                                MultiCommunityEditorMviModel.Intent.SelectImage(
                                                    null,
                                                )
                                            )
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = uiState.name.firstOrNull()?.toString().orEmpty()
                                        .uppercase(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.s))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.s),
                ) {
                    Text(
                        text = LocalXmlStrings.current.multiCommunityEditorCommunities
                    )

                    // search field
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        label = {
                            Text(text = LocalXmlStrings.current.exploreSearchPlaceholder)
                        },
                        singleLine = true,
                        value = uiState.searchText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search,
                        ),
                        onValueChange = { value ->
                            model.reduce(MultiCommunityEditorMviModel.Intent.SetSearch(value))
                        },
                        trailingIcon = {
                            Icon(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        if (uiState.searchText.isNotEmpty()) {
                                            model.reduce(
                                                MultiCommunityEditorMviModel.Intent.SetSearch("")
                                            )
                                        }
                                    },
                                ),
                                imageVector = if (uiState.searchText.isEmpty()) {
                                    Icons.Default.Search
                                } else {
                                    Icons.Default.Clear
                                },
                                contentDescription = null,
                            )
                        },
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.s)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                ) {
                    items(uiState.communities) { communityItem ->
                        val community = communityItem.first
                        val selected = communityItem.second
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CommunityItem(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.background),
                                noPadding = true,
                                community = community,
                                preferNicknames = uiState.preferNicknames,
                            )
                            Checkbox(
                                checked = selected,
                                onCheckedChange = {
                                    model.reduce(
                                        MultiCommunityEditorMviModel.Intent.ToggleCommunity(
                                            community.id
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
