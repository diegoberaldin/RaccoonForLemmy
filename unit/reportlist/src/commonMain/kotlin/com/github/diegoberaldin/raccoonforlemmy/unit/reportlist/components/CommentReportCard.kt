package com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentReportModel

@Composable
internal fun CommentReportCard(
    report: CommentReportModel,
    postLayout: PostLayout = PostLayout.Card,
    modifier: Modifier = Modifier,
    options: List<Option> = emptyList(),
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
    onOpen: (() -> Unit)? = null,
    onOpenImage: ((String) -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    InnerReportCard(
        modifier = modifier,
        reason = report.reason.orEmpty(),
        postLayout = postLayout,
        creator = report.creator,
        date = report.publishDate,
        autoLoadImages = autoLoadImages,
        preferNicknames = preferNicknames,
        options = options,
        onOptionSelected = onOptionSelected,
        onOpen = onOpen,
        originalContent = {
            Column {
                report.originalText?.also { text ->
                    PostCardBody(
                        modifier =
                            Modifier.padding(
                                vertical = Spacing.xs,
                                horizontal = Spacing.xs,
                            ),
                        text = text,
                        autoLoadImages = autoLoadImages,
                        onOpenImage = onOpenImage,
                    )
                }
            }
        },
    )
}
