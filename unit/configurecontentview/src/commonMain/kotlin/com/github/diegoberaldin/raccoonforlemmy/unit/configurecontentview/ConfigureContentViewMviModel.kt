package com.github.diegoberaldin.raccoonforlemmy.unit.configurecontentview

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.CommentBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontScales
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface ConfigureContentViewMviModel :
    MviModel<ConfigureContentViewMviModel.Intent, ConfigureContentViewMviModel.State, ConfigureContentViewMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class ChangePreferUserNicknames(val value: Boolean) : Intent

        data class ChangeFullHeightImages(val value: Boolean) : Intent

        data class ChangeFullWidthImages(val value: Boolean) : Intent

        data object IncrementCommentBarThickness : Intent

        data object DecrementCommentBarThickness : Intent

        data object IncrementCommentIndentAmount : Intent

        data object DecrementCommentIndentAmount : Intent
    }

    data class State(
        val postLayout: PostLayout = PostLayout.Card,
        val commentBarTheme: CommentBarTheme = CommentBarTheme.Blue,
        val commentBarThickness: Int = 1,
        val commentIndentAmount: Int = 2,
        val contentFontScale: ContentFontScales = ContentFontScales(),
        val contentFontFamily: UiFontFamily = UiFontFamily.Poppins,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val postBodyMaxLines: Int? = null,
        val fullHeightImages: Boolean = false,
        val fullWidthImages: Boolean = false,
        val preferUserNicknames: Boolean = true,
    )

    sealed interface Effect
}
