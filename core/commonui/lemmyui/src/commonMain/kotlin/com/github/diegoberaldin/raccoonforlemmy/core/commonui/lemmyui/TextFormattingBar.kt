package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertLink
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.LanguageModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.fontFamilyResource

@Composable
fun TextFormattingBar(
    textFieldValue: TextFieldValue,
    onTextFieldValueChanged: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onSelectImage: (() -> Unit)? = null,
    currentLanguageId: Int? = null,
    availableLanguages: List<LanguageModel> = emptyList(),
    onSelectLanguage: (() -> Unit)? = null,
) {
    val textPlaceholder = "text here"
    val urlPlaceholder = "URL"
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        // bold
        item {
            Icon(
                modifier = Modifier.onClick(
                    onClick = {
                        val selection = textFieldValue.selection
                        val newValue = textFieldValue.let {
                            val newText = buildString {
                                append(it.text.substring(0, selection.start))
                                append("**")
                                if (selection.length == 0) {
                                    append(textPlaceholder)
                                } else {
                                    append(
                                        it.text.substring(
                                            selection.start,
                                            selection.end
                                        )
                                    )
                                }
                                append("**")
                                append(
                                    it.text.substring(
                                        selection.end,
                                        it.text.length
                                    )
                                )
                            }
                            val newSelection = if (selection.length == 0) {
                                TextRange(index = selection.start + textPlaceholder.length + 2)
                            } else {
                                TextRange(
                                    start = it.selection.start + 2,
                                    end = it.selection.end + 2
                                )
                            }
                            it.copy(text = newText, selection = newSelection)
                        }
                        onTextFieldValueChanged(newValue)
                    }),
                imageVector = Icons.Default.FormatBold,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        // italic
        item {
            Icon(
                modifier = Modifier.onClick(
                    onClick = {
                        val selection = textFieldValue.selection
                        val newValue = textFieldValue.let {
                            val newText = buildString {
                                append(it.text.substring(0, selection.start))
                                append("*")
                                if (selection.length == 0) {
                                    append(textPlaceholder)
                                } else {
                                    append(
                                        it.text.substring(
                                            selection.start,
                                            selection.end
                                        )
                                    )
                                }
                                append("*")
                                append(
                                    it.text.substring(
                                        selection.end,
                                        it.text.length
                                    )
                                )
                            }
                            val newSelection = if (selection.length == 0) {
                                TextRange(index = selection.start + textPlaceholder.length + 1)
                            } else {
                                TextRange(
                                    start = it.selection.start + 1,
                                    end = it.selection.end + 1
                                )
                            }
                            it.copy(text = newText, selection = newSelection)
                        }
                        onTextFieldValueChanged(newValue)
                    }),
                imageVector = Icons.Default.FormatItalic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        // strikethrough
        item {
            Icon(
                modifier = Modifier.onClick(
                    onClick = {
                        val selection = textFieldValue.selection
                        val newValue = textFieldValue.let {
                            val newText = buildString {
                                append(it.text.substring(0, selection.start))
                                append("~~")
                                if (selection.length == 0) {
                                    append(textPlaceholder)
                                } else {
                                    append(
                                        it.text.substring(
                                            selection.start,
                                            selection.end
                                        )
                                    )
                                }
                                append("~~")
                                append(
                                    it.text.substring(
                                        selection.end,
                                        it.text.length
                                    )
                                )
                            }
                            val newSelection = if (selection.length == 0) {
                                TextRange(index = selection.start + textPlaceholder.length + 2)
                            } else {
                                TextRange(
                                    start = it.selection.start + 2,
                                    end = it.selection.end + 2
                                )
                            }
                            it.copy(text = newText, selection = newSelection)
                        }
                        onTextFieldValueChanged(newValue)
                    }),
                imageVector = Icons.Default.FormatStrikethrough,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = null,
            )
        }

        // image
        if (onSelectImage != null) {
            item {
                Icon(
                    modifier = Modifier.onClick(
                        onClick = rememberCallback {
                            onSelectImage.invoke()
                        },
                    ),
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        // hyperlink
        item {
            Icon(
                modifier = Modifier.onClick(
                    onClick = {
                        val newValue = textFieldValue.let {
                            val selection = it.selection
                            val newText = buildString {
                                append(it.text.substring(0, selection.start))
                                append("[")
                                if (selection.length == 0) {
                                    append(textPlaceholder)
                                } else {
                                    append(
                                        it.text.substring(
                                            selection.start,
                                            selection.end
                                        )
                                    )
                                }
                                append("](")
                                append(urlPlaceholder)
                                append(")")
                                append(
                                    it.text.substring(
                                        selection.end,
                                        it.text.length
                                    )
                                )
                            }
                            val newSelection = if (selection.length == 0) {
                                TextRange(index = selection.start + textPlaceholder.length + 1)
                            } else {
                                TextRange(
                                    start = it.selection.start + 1,
                                    end = it.selection.end + 1
                                )
                            }
                            it.copy(text = newText, selection = newSelection)
                        }
                        onTextFieldValueChanged(newValue)
                    }),
                imageVector = Icons.Default.InsertLink,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        // inline code
        item {
            Icon(
                modifier = Modifier.onClick(
                    onClick = {
                        val newValue = textFieldValue.let {
                            val selection = it.selection
                            val newText = buildString {
                                append(it.text.substring(0, selection.start))
                                append("`")
                                if (selection.length == 0) {
                                    append(textPlaceholder)
                                } else {
                                    append(
                                        it.text.substring(
                                            selection.start,
                                            selection.end
                                        )
                                    )
                                }
                                append("`")
                                append(
                                    it.text.substring(
                                        selection.end,
                                        it.text.length
                                    )
                                )
                            }
                            val newSelection = if (selection.length == 0) {
                                TextRange(index = selection.start + textPlaceholder.length + 1)
                            } else {
                                TextRange(
                                    start = it.selection.start + 1,
                                    end = it.selection.end + 1
                                )
                            }
                            it.copy(text = newText, selection = newSelection)
                        }
                        onTextFieldValueChanged(newValue)
                    }),
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        // block quote
        item {
            Icon(
                modifier = Modifier.onClick(
                    onClick = {
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
                    }),
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        // bulleted list
        item {
            Icon(
                modifier = Modifier.onClick(
                    onClick = {
                        val newValue = textFieldValue.let {
                            val selection = it.selection
                            val newText = buildString {
                                append(it.text.substring(0, selection.start))
                                append("\n- ")
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
                    }),
                imageVector = Icons.Default.FormatListBulleted,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        // numbered list
        item {
            Icon(
                modifier = Modifier.onClick(
                    onClick = {
                        val newValue = textFieldValue.let {
                            val selection = it.selection
                            val newText = buildString {
                                append(it.text.substring(0, selection.start))
                                append("\n1. ")
                                append(
                                    it.text.substring(
                                        selection.end,
                                        it.text.length
                                    )
                                )
                            }
                            val newSelection = TextRange(index = selection.start + 4)
                            it.copy(text = newText, selection = newSelection)
                        }
                        onTextFieldValueChanged(newValue)
                    }),
                imageVector = Icons.Default.FormatListNumbered,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        // language selection
        if (onSelectLanguage != null) {
            item {
                Box(
                    modifier = Modifier
                        .size(IconSize.m)
                        .border(
                            color = MaterialTheme.colorScheme.onBackground,
                            width = Dp.Hairline,
                            shape = RoundedCornerShape(CornerSize.m)
                        )
                        .clickable(onClick = onSelectLanguage)
                        .padding(Spacing.xxxs),
                    contentAlignment = Alignment.Center,
                ) {
                    val languageCode = availableLanguages.firstOrNull { l ->
                        l.id == currentLanguageId
                    }?.takeIf { l ->
                        l.id > 0 // undetermied language
                    }?.code ?: "λ"
                    Text(
                        text = languageCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = fontFamilyResource(MR.fonts.TitilliumWeb.regular),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}