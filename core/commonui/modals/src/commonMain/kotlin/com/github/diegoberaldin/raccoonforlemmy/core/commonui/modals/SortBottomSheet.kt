package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHeader
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType

private sealed interface SortBottomSheetLevel {
    data object Main : SortBottomSheetLevel

    data object Top : SortBottomSheetLevel
}

class SortBottomSheet(
    private val values: List<Int>,
    private val comments: Boolean = false,
    private val defaultForCommunity: Boolean = false,
    private val expandTop: Boolean = false,
    private val screenKey: String? = null,
) : Screen {
    @Composable
    override fun Content() {
        Column(
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(
                        top = Spacing.s,
                        start = Spacing.s,
                        end = Spacing.s,
                        bottom = Spacing.m,
                    ),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            var level by remember { mutableStateOf<SortBottomSheetLevel>(SortBottomSheetLevel.Main) }
            Crossfade(
                targetState = level,
            ) { currentLevel ->
                when (currentLevel) {
                    SortBottomSheetLevel.Main -> {
                        SortBottomSheetMain(
                            values = values,
                            expandTop = expandTop,
                            comments = comments,
                            defaultForCommunity = defaultForCommunity,
                            screenKey = screenKey,
                            onNavigateUp = {
                                level = SortBottomSheetLevel.Top
                            },
                        )
                    }

                    SortBottomSheetLevel.Top -> {
                        SortBottomSheetTop(
                            comments = comments,
                            defaultForCommunity = defaultForCommunity,
                            screenKey = screenKey,
                            onNavigateDown = {
                                level = SortBottomSheetLevel.Main
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortBottomSheetMain(
    comments: Boolean,
    values: List<Int>,
    expandTop: Boolean = false,
    defaultForCommunity: Boolean = false,
    screenKey: String?,
    onNavigateUp: () -> Unit,
) {
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val notificationCenter = remember { getNotificationCenter() }
    Column {
        BottomSheetHeader(LocalStrings.current.homeSortTitle)
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            for (value in values) {
                val sortValue = value.toSortType()
                Row(
                    modifier =
                        Modifier
                            .padding(
                                horizontal = Spacing.s,
                                vertical = Spacing.s,
                            )
                            .fillMaxWidth()
                            .onClick(
                                onClick = {
                                    if (sortValue == SortType.Top.Generic && expandTop) {
                                        onNavigateUp()
                                    } else {
                                        val event =
                                            if (comments) {
                                                NotificationCenterEvent.ChangeCommentSortType(
                                                    value = sortValue,
                                                    screenKey = screenKey,
                                                )
                                            } else {
                                                NotificationCenterEvent.ChangeSortType(
                                                    value = sortValue,
                                                    defaultForCommunity = defaultForCommunity,
                                                    screenKey = screenKey,
                                                )
                                            }
                                        notificationCenter.send(event)
                                        navigationCoordinator.hideBottomSheet()
                                    }
                                },
                            ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val name =
                        buildString {
                            append(sortValue.toReadableName())
                            if (sortValue == SortType.Top.Generic && expandTop) {
                                append("…")
                            }
                        }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        imageVector =
                            if (sortValue == SortType.Top.Generic && expandTop) {
                                Icons.Default.ChevronRight
                            } else {
                                sortValue.toIcon()
                            },
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    )
                }
            }
        }
    }
}

@Composable
private fun SortBottomSheetTop(
    comments: Boolean,
    values: List<Int> =
        listOf(
            SortType.Top.PastHour,
            SortType.Top.Past6Hours,
            SortType.Top.Past12Hours,
            SortType.Top.Day,
            SortType.Top.Week,
            SortType.Top.Month,
            SortType.Top.Year,
        ).map { it.toInt() },
    defaultForCommunity: Boolean = false,
    screenKey: String?,
    onNavigateDown: () -> Unit,
) {
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val notificationCenter = remember { getNotificationCenter() }

    Column {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            BottomSheetHeader(SortType.Top.Generic.toReadableName() + "…")
            Row(
                modifier = Modifier.padding(start = Spacing.xxs),
            ) {
                Icon(
                    modifier =
                        Modifier
                            .onClick(
                                onClick = {
                                    onNavigateDown()
                                },
                            ),
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            for (value in values) {
                val sortValue = value.toSortType()
                Row(
                    modifier =
                        Modifier
                            .padding(
                                horizontal = Spacing.s,
                                vertical = Spacing.s,
                            )
                            .fillMaxWidth()
                            .onClick(
                                onClick = {
                                    val event =
                                        if (comments) {
                                            NotificationCenterEvent.ChangeCommentSortType(
                                                value = sortValue,
                                                screenKey = screenKey,
                                            )
                                        } else {
                                            NotificationCenterEvent.ChangeSortType(
                                                value = sortValue,
                                                defaultForCommunity = defaultForCommunity,
                                                screenKey = screenKey,
                                            )
                                        }
                                    notificationCenter.send(event)
                                    navigationCoordinator.hideBottomSheet()
                                },
                            ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = sortValue.toReadableName(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        imageVector = sortValue.toIcon(),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    )
                }
            }
        }
    }
}
