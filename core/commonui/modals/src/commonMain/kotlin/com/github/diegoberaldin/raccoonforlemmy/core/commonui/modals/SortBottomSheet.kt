package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType

class SortBottomSheet(
    private val sheetKey: String,
    private val comments: Boolean,
    private val values: List<Int>,
    private val expandTop: Boolean = false,
) : Screen {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(
                    top = Spacing.s,
                    start = Spacing.s,
                    end = Spacing.s,
                    bottom = Spacing.m,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            BottomSheetHandle(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Navigator(
                SortBottomSheetMain(
                    values = values,
                    expandTop = expandTop,
                    comments = comments,
                    sheetKey = sheetKey,
                )
            )
        }
    }
}

internal class SortBottomSheetMain(
    private val sheetKey: String,
    private val comments: Boolean,
    private val values: List<Int>,
    private val expandTop: Boolean = false,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(
                    start = Spacing.s,
                    top = Spacing.s,
                    end = Spacing.s,
                ),
                text = LocalXmlStrings.current.homeSortTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
            ) {
                for (value in values) {
                    val sortValue = value.toSortType()
                    Row(
                        modifier = Modifier
                            .padding(
                                horizontal = Spacing.s,
                                vertical = Spacing.s,
                            )
                            .fillMaxWidth()
                            .onClick(
                                onClick = rememberCallback {
                                    if (sortValue == SortType.Top.Generic && expandTop) {
                                        navigator.push(
                                            SortBottomSheetTop(
                                                comments = comments,
                                                sheetKey = sheetKey,
                                            )
                                        )
                                    } else {
                                        val event = if (comments) {
                                            NotificationCenterEvent.ChangeCommentSortType(
                                                value = sortValue,
                                                key = sheetKey,
                                            )
                                        } else {
                                            NotificationCenterEvent.ChangeSortType(
                                                value = sortValue,
                                                key = sheetKey,
                                            )
                                        }
                                        notificationCenter.send(event)
                                        navigationCoordinator.hideBottomSheet()
                                    }
                                },
                            ),
                    ) {
                        val name = buildString {
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
                            imageVector = if (sortValue == SortType.Top.Generic && expandTop) {
                                Icons.Default.ChevronRight
                            } else sortValue.toIcon(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    }
                }
            }
        }
    }
}

internal class SortBottomSheetTop(
    private val sheetKey: String,
    private val comments: Boolean,
    private val values: List<Int> = listOf(
        SortType.Top.PastHour,
        SortType.Top.Past6Hours,
        SortType.Top.Past12Hours,
        SortType.Top.Day,
        SortType.Top.Week,
        SortType.Top.Month,
        SortType.Top.Year,
    ).map { it.toInt() },
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }

        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.onClick(
                        onClick = rememberCallback {
                            navigator.pop()
                        },
                    ),
                    imageVector = Icons.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(
                        start = Spacing.s,
                        top = Spacing.s,
                        end = Spacing.s,
                    ),
                    text = SortType.Top.Generic.toReadableName() + "…",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
            ) {
                for (value in values) {
                    val sortValue = value.toSortType()
                    Row(
                        modifier = Modifier
                            .padding(
                                horizontal = Spacing.s,
                                vertical = Spacing.s,
                            )
                            .fillMaxWidth()
                            .onClick(
                                onClick = rememberCallback {
                                    val event = if (comments) {
                                        NotificationCenterEvent.ChangeCommentSortType(
                                            value = sortValue,
                                            key = sheetKey,
                                        )
                                    } else {
                                        NotificationCenterEvent.ChangeSortType(
                                            value = sortValue,
                                            key = sheetKey,
                                        )
                                    }
                                    notificationCenter.send(event)
                                    navigationCoordinator.hideBottomSheet()
                                },
                            ),
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
}