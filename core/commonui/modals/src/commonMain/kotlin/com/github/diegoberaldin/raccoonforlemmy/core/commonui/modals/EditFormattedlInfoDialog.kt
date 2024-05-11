package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CreatePostSection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.TextFormattingBar
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFormattedInfoDialog(
    title: String = "",
    value: String = "",
    onClose: ((String?) -> Unit)? = null,
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value))
    }
    var currentSection: CreatePostSection by remember {
        mutableStateOf(CreatePostSection.Edit)
    }
    val themeRepository = remember { getThemeRepository() }
    val contentFontFamily by themeRepository.contentFontFamily.collectAsState()
    val typography = contentFontFamily.toTypography()

    BasicAlertDialog(
        onDismissRequest = {
            onClose?.invoke(null)
        },
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Text(
                text = LocalXmlStrings.current.postActionEdit,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(Spacing.s))

            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                SectionSelector(
                    titles = listOf(
                        LocalXmlStrings.current.createPostTabEditor,
                        LocalXmlStrings.current.createPostTabPreview,
                    ),
                    currentSection = when (currentSection) {
                        CreatePostSection.Preview -> 1
                        else -> 0
                    },
                    onSectionSelected = {
                        val section = when (it) {
                            1 -> CreatePostSection.Preview
                            else -> CreatePostSection.Edit
                        }
                        currentSection = section
                    }
                )

                if (currentSection == CreatePostSection.Edit) {
                    TextFormattingBar(
                        modifier = Modifier.padding(
                            top = Spacing.s,
                            start = Spacing.s,
                            end = Spacing.s,
                        ),
                        textFieldValue = textFieldValue,
                        onTextFieldValueChanged = {
                            textFieldValue = it
                        },
                    )
                    TextField(
                        modifier = Modifier
                            .height(400.dp)
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        label = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        textStyle = typography.bodyMedium,
                        value = textFieldValue,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            autoCorrect = true,
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                        onValueChange = { value ->
                            textFieldValue = value
                        },
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .height(400.dp)
                            .fillMaxWidth()
                    ) {
                        PostCardBody(
                            modifier = Modifier
                                .padding(Spacing.s)
                                .verticalScroll(rememberScrollState()),
                            text = textFieldValue.text,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))
            Button(
                onClick = {
                    onClose?.invoke(textFieldValue.text)
                },
            ) {
                Text(text = LocalXmlStrings.current.buttonConfirm)
            }
        }
    }
}
