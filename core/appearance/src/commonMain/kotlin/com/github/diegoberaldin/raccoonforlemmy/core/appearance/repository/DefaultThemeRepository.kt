package com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository

import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.CommentBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import kotlinx.coroutines.flow.MutableStateFlow


internal class DefaultThemeRepository : ThemeRepository {

    override val uiTheme = MutableStateFlow<UiTheme?>(null)
    override val uiFontFamily = MutableStateFlow(UiFontFamily.Poppins)
    override val uiFontScale = MutableStateFlow(1f)
    override val contentFontScale = MutableStateFlow(ContentFontScales())
    override val contentFontFamily = MutableStateFlow(UiFontFamily.Poppins)
    override val navItemTitles = MutableStateFlow(false)
    override val dynamicColors = MutableStateFlow(false)
    override val customSeedColor = MutableStateFlow<Color?>(null)
    override val upVoteColor = MutableStateFlow<Color?>(null)
    override val downVoteColor = MutableStateFlow<Color?>(null)
    override val replyColor = MutableStateFlow<Color?>(null)
    override val saveColor = MutableStateFlow<Color?>(null)
    override val postLayout = MutableStateFlow<PostLayout>(PostLayout.Card)
    override val commentBarTheme = MutableStateFlow<CommentBarTheme>(CommentBarTheme.Blue)

    override fun changeUiTheme(value: UiTheme?) {
        uiTheme.value = value
    }

    override fun changeUiFontFamily(value: UiFontFamily) {
        uiFontFamily.value = value
    }

    override fun changeUiFontScale(value: Float) {
        uiFontScale.value = value
    }

    override fun changeContentFontScale(value: ContentFontScales) {
        contentFontScale.value = value
    }

    override fun changeContentFontFamily(value: UiFontFamily) {
        contentFontFamily.value = value
    }

    override fun changeNavItemTitles(value: Boolean) {
        navItemTitles.value = value
    }

    override fun changeDynamicColors(value: Boolean) {
        dynamicColors.value = value
    }

    override fun getCommentBarColor(depth: Int, commentBarTheme: CommentBarTheme): Color {
        val colors = getCommentBarColors(commentBarTheme)
        if (colors.isEmpty()) {
            return Color.Transparent
        }
        val index = depth % colors.size
        return colors[index]
    }

    override fun changeCustomSeedColor(color: Color?) {
        customSeedColor.value = color
    }

    override fun changeUpVoteColor(color: Color?) {
        upVoteColor.value = color
    }

    override fun changeDownVoteColor(color: Color?) {
        downVoteColor.value = color
    }

    override fun changeReplyColor(color: Color?) {
        replyColor.value = color
    }

    override fun changeSaveColor(color: Color?) {
        saveColor.value = color
    }

    override fun changePostLayout(value: PostLayout) {
        postLayout.value = value
    }

    override fun changeCommentBarTheme(value: CommentBarTheme) {
        commentBarTheme.value = value
    }

    override fun getCommentBarColors(commentBarTheme: CommentBarTheme): List<Color> =
        when (commentBarTheme) {
            CommentBarTheme.Green -> buildList {
                this += Color(0xFF1B4332)
                this += Color(0xFF2D6A4F)
                this += Color(0xFF40916C)
                this += Color(0xFF52B788)
                this += Color(0xFF74C69D)
                this += Color(0xFF95D5B2)
            }

            CommentBarTheme.Red -> buildList {
                this += Color(0xFF6A040F)
                this += Color(0xFF9D0208)
                this += Color(0xFFD00000)
                this += Color(0xFFDC2F02)
                this += Color(0xFFE85D04)
                this += Color(0xFFF48C06)
            }

            CommentBarTheme.Blue -> buildList {
                this += Color(0xFF012A4A)
                this += Color(0xFF013A63)
                this += Color(0xFF014F86)
                this += Color(0xFF2C7DA0)
                this += Color(0xFF61A5C2)
                this += Color(0xFFA9D6E5)
            }

            CommentBarTheme.Rainbow -> buildList {
                this += Color(0xFF9400D3)
                this += Color(0xFF0000FF)
                this += Color(0xFF00FF00)
                this += Color(0xFFFFFF00)
                this += Color(0xFFFF7F00)
                this += Color(0xFFFF0000)
            }
        }
}
