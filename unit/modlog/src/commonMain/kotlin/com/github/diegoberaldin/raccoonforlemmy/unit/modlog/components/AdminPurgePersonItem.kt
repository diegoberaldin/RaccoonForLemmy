package com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
internal fun AdminPurgePersonItem(
    item: ModlogItem.AdminPurgePerson,
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
                text =
                    buildAnnotatedString {
                        append(LocalXmlStrings.current.modlogItemPersonPurged)
                    },
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
