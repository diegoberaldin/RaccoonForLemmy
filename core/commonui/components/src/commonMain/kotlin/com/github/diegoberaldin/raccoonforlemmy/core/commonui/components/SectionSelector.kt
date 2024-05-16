package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SectionSelector(
    modifier: Modifier = Modifier,
    draggable: Boolean = true,
    titles: List<String> = emptyList(),
    currentSection: Int,
    onSectionSelected: (Int) -> Unit,
) {
    var isTowardsStart by remember { mutableStateOf(false) }
    val draggableState =
        remember {
            DraggableState { delta ->
                isTowardsStart = delta > 0
            }
        }
    TabRow(
        modifier = modifier,
        selectedTabIndex = currentSection,
        tabs = {
            titles.forEachIndexed { i, title ->
                Tab(
                    modifier =
                        Modifier.then(
                            if (draggable) {
                                Modifier.draggable(
                                    state = draggableState,
                                    orientation = Orientation.Horizontal,
                                    onDragStopped = {
                                        if (isTowardsStart) {
                                            onSectionSelected((currentSection - 1).coerceAtLeast(0))
                                        } else {
                                            onSectionSelected((currentSection + 1).coerceAtMost(titles.lastIndex))
                                        }
                                    },
                                )
                            } else {
                                Modifier
                            },
                        ),
                    selected = i == currentSection,
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    onClick = {
                        onSectionSelected(i)
                    },
                )
            }
        },
    )
}
