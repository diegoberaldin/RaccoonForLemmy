package com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun ModAddItem(
    item: ModlogItem.ModAdd,
    modifier: Modifier = Modifier,
    autoLoadImages: Boolean = true,
    postLayout: PostLayout = PostLayout.Card,
    onOpenUser: ((UserModel) -> Unit)? = null,
) {
    InnerModlogItem(
        modifier = modifier,
        autoLoadImages = autoLoadImages,
        date = item.date,
        postLayout = postLayout,
        moderator = item.moderator,
        onOpenUser = onOpenUser,
        onOpen = rememberCallback {
            item.user?.also {
                onOpenUser?.invoke(it)
            }
        },
        innerContent = {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        val name = item.user?.name.orEmpty()
                        val host = item.user?.host.orEmpty()
                        append(name)
                        if (host.isNotEmpty()) {
                            append("@")
                            append(host)
                        }
                    }
                    append(" ")
                    if (item.removed) {
                        append(stringResource(MR.strings.modlog_item_mod_removed))
                    } else {
                        append(stringResource(MR.strings.modlog_item_mod_added))
                    }
                },
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}