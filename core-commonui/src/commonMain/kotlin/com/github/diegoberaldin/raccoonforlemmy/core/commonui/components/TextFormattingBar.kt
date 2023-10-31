package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertLink
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick

@Composable
fun TextFormattingBar(
    textFieldValue: TextFieldValue,
    onTextFieldValueChanged: (TextFieldValue) -> Unit,
    onSelectImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        Icon(
            modifier = Modifier.onClick {
                val selection = textFieldValue.selection
                val newValue = textFieldValue.let {
                    val newText = buildString {
                        append(it.text.substring(0, selection.start))
                        append("**")
                        append(
                            it.text.substring(
                                selection.start,
                                selection.end
                            )
                        )
                        append("**")
                        append(
                            it.text.substring(
                                selection.end,
                                it.text.length
                            )
                        )
                    }
                    val newSelection = if (selection.length == 0) {
                        TextRange(index = selection.start + 2)
                    } else {
                        TextRange(start = it.selection.start + 2, end = it.selection.end + 2)
                    }
                    it.copy(text = newText, selection = newSelection)
                }
                onTextFieldValueChanged(newValue)
            },
            imageVector = Icons.Default.FormatBold,
            contentDescription = null,
        )
        Icon(
            modifier = Modifier.onClick {
                val selection = textFieldValue.selection
                val newValue = textFieldValue.let {
                    val newText = buildString {
                        append(it.text.substring(0, selection.start))
                        append("*")
                        append(
                            it.text.substring(
                                selection.start,
                                selection.end
                            )
                        )
                        append("*")
                        append(
                            it.text.substring(
                                selection.end,
                                it.text.length
                            )
                        )
                    }
                    val newSelection = if (selection.length == 0) {
                        TextRange(index = selection.start + 1)
                    } else {
                        TextRange(start = it.selection.start + 1, end = it.selection.end + 1)
                    }
                    it.copy(text = newText, selection = newSelection)
                }
                onTextFieldValueChanged(newValue)
            },
            imageVector = Icons.Default.FormatItalic,
            contentDescription = null,
        )
        Icon(
            modifier = Modifier.onClick {
                val selection = textFieldValue.selection
                val newValue = textFieldValue.let {
                    val newText = buildString {
                        append(it.text.substring(0, selection.start))
                        append("~~")
                        append(
                            it.text.substring(
                                selection.start,
                                selection.end
                            )
                        )
                        append("~~")
                        append(
                            it.text.substring(
                                selection.end,
                                it.text.length
                            )
                        )
                    }
                    val newSelection = if (selection.length == 0) {
                        TextRange(index = selection.start + 2)
                    } else {
                        TextRange(start = it.selection.start + 2, end = it.selection.end + 2)
                    }
                    it.copy(text = newText, selection = newSelection)
                }
                onTextFieldValueChanged(newValue)
            },
            imageVector = Icons.Default.FormatStrikethrough,
            contentDescription = null,
        )
        Icon(
            modifier = Modifier.onClick {
                onSelectImage()
            },
            imageVector = Icons.Default.Image,
            contentDescription = null,
        )
        Icon(
            modifier = Modifier.onClick {
                val newValue = textFieldValue.let {
                    val selection = it.selection
                    val newText = buildString {
                        append(it.text.substring(0, selection.start))
                        append(" [](url)")
                        append(
                            it.text.substring(
                                selection.end,
                                it.text.length
                            )
                        )
                    }
                    val newSelection = TextRange(index = selection.start + 2)
                    it.copy(text = newText, selection = newSelection)
                }
                onTextFieldValueChanged(newValue)
            },
            imageVector = Icons.Default.InsertLink,
            contentDescription = null,
        )
        Icon(
            modifier = Modifier.onClick {
                val newValue = textFieldValue.let {
                    val selection = it.selection
                    val newText = buildString {
                        append(it.text.substring(0, selection.start))
                        append("`")
                        append(
                            it.text.substring(
                                selection.start,
                                selection.end
                            )
                        )
                        append("`")
                        append(
                            it.text.substring(
                                selection.end,
                                it.text.length
                            )
                        )
                    }
                    val newSelection = if (selection.length == 0) {
                        TextRange(index = selection.start + 1)
                    } else {
                        TextRange(start = it.selection.start + 1, end = it.selection.end + 1)
                    }
                    it.copy(text = newText, selection = newSelection)
                }
                onTextFieldValueChanged(newValue)
            },
            imageVector = Icons.Default.Code,
            contentDescription = null,
        )
        Icon(
            modifier = Modifier.onClick {
                val newValue = textFieldValue.let {
                    val selection = it.selection
                    val newText = buildString {
                        append(it.text.substring(0, selection.start))
                        append("\n> ")
                        append(
                            it.text.substring(
                                selection.end,
                                it.text.length
                            )
                        )
                    }
                    val newSelection = TextRange(index = selection.start + 3)
                    it.copy(text = newText, selection = newSelection)
                }
                onTextFieldValueChanged(newValue)
            },
            imageVector = Icons.Default.FormatQuote,
            contentDescription = null,
        )
    }
}