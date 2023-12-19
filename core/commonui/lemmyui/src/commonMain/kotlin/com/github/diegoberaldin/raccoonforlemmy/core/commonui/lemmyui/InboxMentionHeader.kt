package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun InboxCardHeader(
    mention: PersonMentionModel,
    type: InboxCardType,
) {
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
    val header = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fullColor)) {
            append(mention.creator.name)
        }
        append(" ")
        withStyle(SpanStyle(color = ancillaryColor)) {
            when (type) {
                InboxCardType.Mention -> {
                    append(stringResource(MR.strings.inbox_item_mention))
                }

                InboxCardType.Reply -> {
                    if (mention.isOwnPost) {
                        append(stringResource(MR.strings.inbox_item_reply_post))
                    } else {
                        append(stringResource(MR.strings.inbox_item_reply_comment))
                    }
                }
            }

            append(" ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fullColor)) {
                append(mention.post.title)
            }
        }
    }
    Row(
        modifier = Modifier.padding(end = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Spacing.xs, horizontal = Spacing.xs),
            text = header,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (!mention.read) {
            Icon(
                modifier = Modifier.size(IconSize.xs),
                imageVector = Icons.Filled.FiberManualRecord,
                contentDescription = null,
                tint = ancillaryColor,
            )
        }
    }
}
