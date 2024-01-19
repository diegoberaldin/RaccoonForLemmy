package com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsHeader
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipeDirection
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipeTarget
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions.ui.components.ConfigureActionItem
import com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions.ui.modals.SelectActionOnSwipeBottomSheet
import dev.icerock.moko.resources.compose.stringResource

class ConfigureSwipeActionsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ConfigureSwipeActionsMviModel>()
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = stringResource(MR.strings.settings_configure_swipe_actions),
                        )
                    },
                    navigationIcon = {
                        if (navigationCoordinator.canPop.value) {
                            Image(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        navigationCoordinator.popScreen()
                                    },
                                ),
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .then(
                        if (settings.hideNavigationBarWhileScrolling) {
                            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            Modifier
                        }
                    )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    // posts
                    item {
                        SettingsHeader(
                            title = stringResource(MR.strings.explore_result_type_posts),
                            rightButton = Icons.Default.RestartAlt,
                            onRightButtonClicked = rememberCallback(model) {
                                model.reduce(ConfigureSwipeActionsMviModel.Intent.ResetActionsPosts)
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = Spacing.xxs,
                                    horizontal = Spacing.s,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(MR.strings.configure_actions_side_start),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (uiState.availableOptionsPosts.isNotEmpty() && uiState.actionsOnSwipeToStartPosts.size < 2) {
                                Icon(
                                    modifier = Modifier.size(IconSize.m)
                                        .onClick(
                                            onClick = rememberCallback {
                                                val sheet = SelectActionOnSwipeBottomSheet(
                                                    values = uiState.availableOptionsPosts,
                                                    direction = ActionOnSwipeDirection.ToStart,
                                                    target = ActionOnSwipeTarget.Posts,
                                                )
                                                navigationCoordinator.showBottomSheet(sheet)
                                            },
                                        ),
                                    imageVector = Icons.Outlined.AddCircle,
                                    contentDescription = null,
                                    tint = ancillaryColor,
                                )
                            }
                        }
                    }
                    itemsIndexed(uiState.actionsOnSwipeToStartPosts) { idx, action ->
                        ConfigureActionItem(
                            icon = when (idx) {
                                1 -> Icons.Default.KeyboardDoubleArrowLeft
                                else -> Icons.Default.KeyboardArrowLeft
                            },
                            action = action,
                            options = buildList {
                                this += Option(
                                    OptionId.Remove,
                                    stringResource(MR.strings.comment_action_delete)
                                )
                            },
                            onOptionSelected = { optionId ->
                                when (optionId) {
                                    OptionId.Remove -> {
                                        model.reduce(
                                            ConfigureSwipeActionsMviModel.Intent.DeleteActionPosts(
                                                value = action,
                                                direction = ActionOnSwipeDirection.ToStart,
                                            )
                                        )
                                    }

                                    else -> Unit
                                }
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = Spacing.xxs,
                                    horizontal = Spacing.s,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(MR.strings.configure_actions_side_end),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (uiState.availableOptionsPosts.isNotEmpty() && uiState.actionsOnSwipeToEndPosts.size < 2) {
                                Icon(
                                    modifier = Modifier.size(IconSize.m)
                                        .onClick(
                                            onClick = rememberCallback {
                                                val sheet = SelectActionOnSwipeBottomSheet(
                                                    values = uiState.availableOptionsPosts,
                                                    direction = ActionOnSwipeDirection.ToEnd,
                                                    target = ActionOnSwipeTarget.Posts,
                                                )
                                                navigationCoordinator.showBottomSheet(sheet)
                                            },
                                        ),
                                    imageVector = Icons.Outlined.AddCircle,
                                    contentDescription = null,
                                    tint = ancillaryColor,
                                )
                            }
                        }
                    }
                    itemsIndexed(uiState.actionsOnSwipeToEndPosts) { idx, action ->
                        ConfigureActionItem(
                            icon = when (idx) {
                                1 -> Icons.Default.KeyboardDoubleArrowRight
                                else -> Icons.Default.KeyboardArrowRight
                            },
                            action = action,
                            options = buildList {
                                this += Option(
                                    OptionId.Remove,
                                    stringResource(MR.strings.comment_action_delete)
                                )
                            },
                            onOptionSelected = { optionId ->
                                when (optionId) {
                                    OptionId.Remove -> {
                                        model.reduce(
                                            ConfigureSwipeActionsMviModel.Intent.DeleteActionPosts(
                                                value = action,
                                                direction = ActionOnSwipeDirection.ToEnd,
                                            )
                                        )
                                    }

                                    else -> Unit
                                }
                            },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(Spacing.s))
                    }

                    // comments
                    item {
                        SettingsHeader(
                            title = stringResource(MR.strings.explore_result_type_comments),
                            rightButton = Icons.Default.RestartAlt,
                            onRightButtonClicked = rememberCallback(model) {
                                model.reduce(ConfigureSwipeActionsMviModel.Intent.ResetActionsComments)
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = Spacing.xxs,
                                    horizontal = Spacing.s,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(MR.strings.configure_actions_side_start),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (uiState.availableOptionsComments.isNotEmpty() && uiState.actionsOnSwipeToStartComments.size < 2) {
                                Icon(
                                    modifier = Modifier.size(IconSize.m)
                                        .onClick(
                                            onClick = rememberCallback {
                                                val sheet = SelectActionOnSwipeBottomSheet(
                                                    values = uiState.availableOptionsComments,
                                                    direction = ActionOnSwipeDirection.ToStart,
                                                    target = ActionOnSwipeTarget.Comments,
                                                )
                                                navigationCoordinator.showBottomSheet(sheet)
                                            },
                                        ),
                                    imageVector = Icons.Outlined.AddCircle,
                                    contentDescription = null,
                                    tint = ancillaryColor,
                                )
                            }
                        }
                    }
                    itemsIndexed(uiState.actionsOnSwipeToStartComments) { idx, action ->
                        ConfigureActionItem(
                            icon = when (idx) {
                                1 -> Icons.Default.KeyboardDoubleArrowLeft
                                else -> Icons.Default.KeyboardArrowLeft
                            },
                            action = action,
                            options = buildList {
                                this += Option(
                                    OptionId.Remove,
                                    stringResource(MR.strings.comment_action_delete)
                                )
                            },
                            onOptionSelected = { optionId ->
                                when (optionId) {
                                    OptionId.Remove -> {
                                        model.reduce(
                                            ConfigureSwipeActionsMviModel.Intent.DeleteActionComments(
                                                value = action,
                                                direction = ActionOnSwipeDirection.ToStart,
                                            )
                                        )
                                    }

                                    else -> Unit
                                }
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = Spacing.xxs,
                                    horizontal = Spacing.s,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(MR.strings.configure_actions_side_end),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (uiState.availableOptionsComments.isNotEmpty() && uiState.actionsOnSwipeToEndComments.size < 2) {
                                Icon(
                                    modifier = Modifier.size(IconSize.m)
                                        .onClick(
                                            onClick = rememberCallback {
                                                val sheet = SelectActionOnSwipeBottomSheet(
                                                    values = uiState.availableOptionsComments,
                                                    direction = ActionOnSwipeDirection.ToEnd,
                                                    target = ActionOnSwipeTarget.Comments,
                                                )
                                                navigationCoordinator.showBottomSheet(sheet)
                                            },
                                        ),
                                    imageVector = Icons.Outlined.AddCircle,
                                    contentDescription = null,
                                    tint = ancillaryColor,
                                )
                            }
                        }
                    }
                    itemsIndexed(uiState.actionsOnSwipeToEndComments) { idx, action ->
                        ConfigureActionItem(
                            icon = when (idx) {
                                1 -> Icons.Default.KeyboardDoubleArrowRight
                                else -> Icons.Default.KeyboardArrowRight
                            },
                            action = action,
                            options = buildList {
                                this += Option(
                                    OptionId.Remove,
                                    stringResource(MR.strings.comment_action_delete)
                                )
                            },
                            onOptionSelected = { optionId ->
                                when (optionId) {
                                    OptionId.Remove -> {
                                        model.reduce(
                                            ConfigureSwipeActionsMviModel.Intent.DeleteActionComments(
                                                value = action,
                                                direction = ActionOnSwipeDirection.ToEnd,
                                            )
                                        )
                                    }

                                    else -> Unit
                                }
                            },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(Spacing.s))
                    }

                    // inbox
                    item {
                        SettingsHeader(
                            title = stringResource(MR.strings.navigation_inbox),
                            rightButton = Icons.Default.RestartAlt,
                            onRightButtonClicked = rememberCallback(model) {
                                model.reduce(ConfigureSwipeActionsMviModel.Intent.ResetActionsInbox)
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = Spacing.xxs,
                                    horizontal = Spacing.s,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(MR.strings.configure_actions_side_start),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (uiState.availableOptionsInbox.isNotEmpty() && uiState.actionsOnSwipeToStartInbox.size < 2) {
                                Icon(
                                    modifier = Modifier.size(IconSize.m)
                                        .onClick(
                                            onClick = rememberCallback {
                                                val sheet = SelectActionOnSwipeBottomSheet(
                                                    values = uiState.availableOptionsInbox,
                                                    direction = ActionOnSwipeDirection.ToStart,
                                                    target = ActionOnSwipeTarget.Inbox,
                                                )
                                                navigationCoordinator.showBottomSheet(sheet)
                                            },
                                        ),
                                    imageVector = Icons.Outlined.AddCircle,
                                    contentDescription = null,
                                    tint = ancillaryColor,
                                )
                            }
                        }
                    }
                    itemsIndexed(uiState.actionsOnSwipeToStartInbox) { idx, action ->
                        ConfigureActionItem(
                            icon = when (idx) {
                                1 -> Icons.Default.KeyboardDoubleArrowLeft
                                else -> Icons.Default.KeyboardArrowLeft
                            },
                            action = action,
                            options = buildList {
                                this += Option(
                                    OptionId.Remove,
                                    stringResource(MR.strings.comment_action_delete)
                                )
                            },
                            onOptionSelected = { optionId ->
                                when (optionId) {
                                    OptionId.Remove -> {
                                        model.reduce(
                                            ConfigureSwipeActionsMviModel.Intent.DeleteActionInbox(
                                                value = action,
                                                direction = ActionOnSwipeDirection.ToStart,
                                            )
                                        )
                                    }

                                    else -> Unit
                                }
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = Spacing.xxs,
                                    horizontal = Spacing.s,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(MR.strings.configure_actions_side_end),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (uiState.availableOptionsInbox.isNotEmpty() && uiState.actionsOnSwipeToEndInbox.size < 2) {
                                Icon(
                                    modifier = Modifier.size(IconSize.m)
                                        .onClick(
                                            onClick = rememberCallback {
                                                val sheet = SelectActionOnSwipeBottomSheet(
                                                    values = uiState.availableOptionsInbox,
                                                    direction = ActionOnSwipeDirection.ToEnd,
                                                    target = ActionOnSwipeTarget.Inbox,
                                                )
                                                navigationCoordinator.showBottomSheet(sheet)
                                            },
                                        ),
                                    imageVector = Icons.Outlined.AddCircle,
                                    contentDescription = null,
                                    tint = ancillaryColor,
                                )
                            }
                        }
                    }
                    itemsIndexed(uiState.actionsOnSwipeToEndInbox) { idx, action ->
                        ConfigureActionItem(
                            icon = when (idx) {
                                1 -> Icons.Default.KeyboardDoubleArrowRight
                                else -> Icons.Default.KeyboardArrowRight
                            },
                            action = action,
                            options = buildList {
                                this += Option(
                                    OptionId.Remove,
                                    stringResource(MR.strings.comment_action_delete)
                                )
                            },
                            onOptionSelected = { optionId ->
                                when (optionId) {
                                    OptionId.Remove -> {
                                        model.reduce(
                                            ConfigureSwipeActionsMviModel.Intent.DeleteActionInbox(
                                                value = action,
                                                direction = ActionOnSwipeDirection.ToEnd,
                                            )
                                        )
                                    }

                                    else -> Unit
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
