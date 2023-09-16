package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset

@Composable
actual fun CustomDropDown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier,
    offset: DpOffset,
    content: @Composable (ColumnScope.() -> Unit),
) {
    DropdownMenu(
        modifier = modifier,
        offset = offset,
        expanded = expanded,
        content = content,
        onDismissRequest = onDismiss,
    )
}
