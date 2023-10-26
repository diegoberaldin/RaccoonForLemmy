package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RawContentDialog(
    title: String? = null,
    url: String? = null,
    text: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .heightIn(max = 600.dp)
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(vertical = Spacing.s, horizontal = Spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = stringResource(MR.strings.dialog_title_raw_content),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(Spacing.s))
            title?.takeIf { it.trim().isNotEmpty() }?.also {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(MR.strings.dialog_raw_content_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                SelectionContainer {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = it,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            url?.takeIf { it.trim().isNotEmpty() }?.also {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(MR.strings.dialog_raw_content_url),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                SelectionContainer {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = it,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            text?.takeIf { it.trim().isNotEmpty() }?.also {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(MR.strings.dialog_raw_content_text),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                SelectionContainer {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = it,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))
            Button(
                onClick = {
                    onDismiss?.invoke()
                },
            ) {
                Text(text = stringResource(MR.strings.button_close))
            }
        }
    }
}
