package com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent

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
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.BasicAlertDialog
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
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.getShareHelper
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.di.getCustomTextToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RawContentDialog(
    publishDate: String? = null,
    updateDate: String? = null,
    title: String? = null,
    url: String? = null,
    text: String? = null,
    isLogged: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    onQuote: ((String?) -> Unit)? = null,
    upVotes: Int? = null,
    downVotes: Int? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    val shareHelper = remember { getShareHelper() }
    val onShareLambda = rememberCallback {
        val query = clipboardManager.getText()?.text.orEmpty()
        shareHelper.share(query)
    }
    val onQuoteLambda = rememberCallback {
        val query = clipboardManager.getText()?.text.orEmpty()
        onQuote?.invoke(query)
    }
    val quoteActionLabel = LocalXmlStrings.current.actionQuote
    val shareActionLabel = LocalXmlStrings.current.postActionShare
    val fullColor = MaterialTheme.colorScheme.onBackground

    BasicAlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
    ) {
        Column(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
                .padding(vertical = Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = LocalXmlStrings.current.dialogTitleRawContent,
                style = MaterialTheme.typography.titleMedium,
                color = fullColor,
            )
            Spacer(modifier = Modifier.height(Spacing.s))
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = Spacing.s, horizontal = Spacing.m)
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                title?.takeIf { it.trim().isNotEmpty() }?.also {
                    item {
                        Column {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = LocalXmlStrings.current.dialogRawContentTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = fullColor,
                            )
                            CompositionLocalProvider(
                                LocalTextToolbar provides getCustomTextToolbar(
                                    isLogged = isLogged,
                                    quoteActionLabel = quoteActionLabel,
                                    shareActionLabel = shareActionLabel,
                                    onShare = onShareLambda,
                                    onQuote = onQuoteLambda,
                                ),
                            ) {
                                SelectionContainer {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = it,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                        ),
                                        color = fullColor,
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
                                text = LocalXmlStrings.current.dialogRawContentUrl,
                                style = MaterialTheme.typography.titleMedium,
                                color = fullColor,
                            )
                            CompositionLocalProvider(
                                LocalTextToolbar provides getCustomTextToolbar(
                                    isLogged = isLogged,
                                    quoteActionLabel = quoteActionLabel,
                                    shareActionLabel = shareActionLabel,
                                    onShare = onShareLambda,
                                    onQuote = onQuoteLambda,
                                ),
                            ) {
                                SelectionContainer {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                        ),
                                        color = fullColor,
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
                                text = LocalXmlStrings.current.dialogRawContentText,
                                style = MaterialTheme.typography.titleMedium,
                                color = fullColor,
                            )

                            CompositionLocalProvider(
                                LocalTextToolbar provides getCustomTextToolbar(
                                    isLogged = isLogged,
                                    quoteActionLabel = quoteActionLabel,
                                    shareActionLabel = shareActionLabel,
                                    onShare = onShareLambda,
                                    onQuote = onQuoteLambda,
                                ),
                            ) {
                                SelectionContainer {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                        ),
                                        color = fullColor,
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    modifier = Modifier.size(IconSize.s).padding(0.5.dp),
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = fullColor,
                                )
                                Text(
                                    modifier = Modifier.weight(1f).padding(start = Spacing.xxs),
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                    ),
                                    color = fullColor,
                                )
                            }
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
                                modifier = Modifier.size(IconSize.s),
                                imageVector = Icons.Default.Update,
                                contentDescription = null,
                                tint = fullColor,
                            )
                            Text(
                                modifier = Modifier.weight(1f),
                                text = it,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                                color = fullColor,
                            )
                        }
                    }
                }
                if (upVotes != null && downVotes != null) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            Icon(
                                modifier = Modifier.size(IconSize.m).padding(end = 3.5.dp),
                                imageVector = Icons.Default.ArrowCircleUp,
                                contentDescription = null,
                                tint = fullColor,
                            )
                            Text(
                                text = "$upVotes",
                                style = MaterialTheme.typography.labelLarge,
                                color = fullColor,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                modifier = Modifier.size(IconSize.m).padding(end = 3.5.dp),
                                imageVector = Icons.Default.ArrowCircleDown,
                                contentDescription = null,
                                tint = fullColor,
                            )
                            Text(
                                text = "$downVotes",
                                style = MaterialTheme.typography.labelLarge,
                                color = fullColor,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            val totalVotes = upVotes + downVotes
                            val percVote =
                                if (totalVotes == 0) 0.0 else upVotes.toDouble() / totalVotes
                            val percentage = "${(percVote * 100).toInt()}"
                            Icon(
                                modifier = Modifier.size(IconSize.m).padding(end = 3.5.dp),
                                imageVector = Icons.Default.Percent,
                                contentDescription = null,
                                tint = fullColor,
                            )
                            Text(
                                text = percentage,
                                style = MaterialTheme.typography.labelLarge,
                                color = fullColor,
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
                Text(text = LocalXmlStrings.current.buttonClose)
            }
        }
    }
}
