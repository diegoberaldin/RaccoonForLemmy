package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class SortBottomSheet(
    private val values: List<SortType> = listOf(
        SortType.Active,
        SortType.Hot,
        SortType.New,
        SortType.NewComments,
        SortType.MostComments,
        SortType.Top.Generic,
        SortType.Old,
    ),
    private val expandTop: Boolean = false,
    private val onSelected: (SortType) -> Unit,
    private val onHide: () -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(
                top = Spacing.s,
                start = Spacing.s,
                end = Spacing.s,
                bottom = Spacing.m,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally).width(60.dp).height(1.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(1.dp),
                    ),
            )
            Navigator(
                SortBottomSheetMain(
                    values = values,
                    expandTop = expandTop,
                    onSelected = {
                        onSelected(it)
                        onHide()
                    }
                )
            )
        }
    }
}

internal class SortBottomSheetMain(
    private val values: List<SortType>,
    private val expandTop: Boolean = false,
    private val onSelected: (SortType) -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Column {
            Text(
                modifier = Modifier.padding(start = Spacing.s, top = Spacing.s),
                text = stringResource(MR.strings.home_sort_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
            ) {
                for (value in values) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = Spacing.s,
                            vertical = Spacing.m,
                        )
                            .fillMaxWidth()
                            .onClick {
                                if (value == SortType.Top.Generic == expandTop) {
                                    navigator.push(
                                        SortBottomSheetTop(
                                            onSelected = {
                                                onSelected(it)
                                            }
                                        )
                                    )
                                } else {
                                    onSelected(value)
                                }
                            },
                    ) {
                        val name = buildString {
                            append(value.toReadableName())
                            if (value == SortType.Top.Generic && expandTop) {
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
                            imageVector = if (value == SortType.Top.Generic && expandTop) {
                                Icons.Default.ChevronRight
                            } else value.toIcon(),
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
    private val values: List<SortType> = listOf(
        SortType.Top.PastHour,
        SortType.Top.Past6Hours,
        SortType.Top.Past12Hours,
        SortType.Top.Day,
        SortType.Top.Week,
        SortType.Top.Month,
        SortType.Top.Year,
    ),
    private val onSelected: (SortType) -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Icon(
                    modifier = Modifier.onClick {
                        navigator.pop()
                    },
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(start = Spacing.s, top = Spacing.s),
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
                    Row(
                        modifier = Modifier.padding(
                            horizontal = Spacing.s,
                            vertical = Spacing.m,
                        )
                            .fillMaxWidth()
                            .onClick {
                                onSelected(value)
                            },
                    ) {
                        Text(
                            text = value.toReadableName(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}