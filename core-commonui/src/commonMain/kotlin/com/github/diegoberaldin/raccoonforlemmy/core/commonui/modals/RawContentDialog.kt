package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCustomTextToolbar
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.getShareHelper
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RawContentDialog(
    publishDate: String? = null,
    updateDate: String? = null,
    title: String? = null,
    url: String? = null,
    text: String? = null,
    onDismiss: (() -> Unit)? = null,
    onQuote: ((String?) -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    val shareHelper = remember { getShareHelper() }
    val onShareLambda = rememberCallback {
        val query = clipboardManager.getText()?.text.orEmpty()
        shareHelper.share(query, "text/plain")
    }
    val onQuoteLambda = rememberCallback {
        val query = clipboardManager.getText()?.text.orEmpty()
        onQuote?.invoke(query)
    }

    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
    ) {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(vertical = Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(MR.strings.dialog_title_raw_content),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(Spacing.s))
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = Spacing.s, horizontal = Spacing.m)
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                title?.takeIf { it.trim().isNotEmpty() }?.also {
                    item {
                        Column {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(MR.strings.dialog_raw_content_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            CompositionLocalProvider(
                                LocalTextToolbar provides getCustomTextToolbar(
                                    onShare = onShareLambda,
                                    onQuote = onQuoteLambda,
                                )
                            ) {
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
                        }
                    }
                }
                url?.takeIf { it.trim().isNotEmpty() }?.also {
                    item {
                        Column {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(MR.strings.dialog_raw_content_url),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            CompositionLocalProvider(
                                LocalTextToolbar provides getCustomTextToolbar(
                                    onShare = onShareLambda,
                                    onQuote = onQuoteLambda,
                                )
                            ) {
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
                        }
                    }
                }
                text?.takeIf { it.trim().isNotEmpty() }?.also {
                    item {
                        Column {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(MR.strings.dialog_raw_content_text),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            CompositionLocalProvider(
                                LocalTextToolbar provides getCustomTextToolbar(
                                    onShare = onShareLambda,
                                    onQuote = onQuoteLambda,
                                )
                            ) {
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
                        }
                    }
                }

                publishDate?.takeIf { it.trim().isNotEmpty() }?.also {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                        ) {
                            Icon(
                                modifier = Modifier.size(IconSize.m).padding(4.5.dp),
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                modifier = Modifier.weight(1f),
                                text = it,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
                updateDate?.takeIf { it.trim().isNotEmpty() }?.also {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                        ) {
                            Icon(
                                modifier = Modifier.size(IconSize.m).padding(4.5.dp),
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                modifier = Modifier.weight(1f),
                                text = it,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
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
