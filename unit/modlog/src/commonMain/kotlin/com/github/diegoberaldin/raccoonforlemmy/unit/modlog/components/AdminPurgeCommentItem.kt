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
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ellipsize
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
internal fun AdminPurgeCommentItem(
    item: ModlogItem.AdminPurgeComment,
    modifier: Modifier = Modifier,
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
    postLayout: PostLayout = PostLayout.Card,
    onOpenUser: ((UserModel) -> Unit)? = null,
) {
    InnerModlogItem(
        modifier = modifier,
        autoLoadImages = autoLoadImages,
        preferNicknames = preferNicknames,
        date = item.date,
        postLayout = postLayout,
        moderator = item.admin,
        onOpenUser = onOpenUser,
        innerContent = {
            Text(
                text = buildAnnotatedString {
                    append(LocalXmlStrings.current.modlogItemCommentPurged)
                    if (item.post != null) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(item.post?.title.ellipsize())
                        }
                    }
                },
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
