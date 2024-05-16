package com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.CommentBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import kotlinx.coroutines.flow.StateFlow

@Stable
interface ThemeRepository {
    val uiTheme: StateFlow<UiTheme?>
    val uiFontFamily: StateFlow<UiFontFamily>
    val uiFontScale: StateFlow<Float>
    val contentFontScale: StateFlow<ContentFontScales>
    val contentFontFamily: StateFlow<UiFontFamily>
    val navItemTitles: StateFlow<Boolean>
    val dynamicColors: StateFlow<Boolean>
    val customSeedColor: StateFlow<Color?>
    val upVoteColor: StateFlow<Color?>
    val downVoteColor: StateFlow<Color?>
    val replyColor: StateFlow<Color?>
    val saveColor: StateFlow<Color?>
    val postLayout: StateFlow<PostLayout>
    val commentBarTheme: StateFlow<CommentBarTheme>

    fun changeUiTheme(value: UiTheme?)

    fun changeUiFontFamily(value: UiFontFamily)

    fun changeUiFontScale(value: Float)

    fun changeContentFontScale(value: ContentFontScales)

    fun changeContentFontFamily(value: UiFontFamily)

    fun changeNavItemTitles(value: Boolean)

    fun changeDynamicColors(value: Boolean)

    fun getCommentBarColor(
        depth: Int,
        commentBarTheme: CommentBarTheme,
    ): Color

    fun changeCustomSeedColor(color: Color?)

    fun changeUpVoteColor(color: Color?)

    fun changeDownVoteColor(color: Color?)

    fun changeReplyColor(color: Color?)

    fun changeSaveColor(color: Color?)

    fun changePostLayout(value: PostLayout)

    fun changeCommentBarTheme(value: CommentBarTheme)

    fun getCommentBarColors(commentBarTheme: CommentBarTheme): List<Color>
}
