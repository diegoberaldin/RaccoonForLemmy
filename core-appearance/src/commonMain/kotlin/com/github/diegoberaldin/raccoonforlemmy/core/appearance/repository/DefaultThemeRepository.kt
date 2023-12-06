package com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository

import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import kotlinx.coroutines.flow.MutableStateFlow


internal class DefaultThemeRepository : ThemeRepository {

    override val uiTheme = MutableStateFlow<UiTheme?>(null)
    override val uiFontFamily = MutableStateFlow(UiFontFamily.Poppins)
    override val uiFontScale = MutableStateFlow(1f)
    override val contentFontScale = MutableStateFlow(1f)
    override val navItemTitles = MutableStateFlow(false)
    override val dynamicColors = MutableStateFlow(false)
    override val customSeedColor = MutableStateFlow<Color?>(null)
    override val upvoteColor = MutableStateFlow<Color?>(null)
    override val downvoteColor = MutableStateFlow<Color?>(null)
    override val postLayout = MutableStateFlow<PostLayout>(PostLayout.Card)

    override fun changeUiTheme(value: UiTheme?) {
        uiTheme.value = value
    }

    override fun changeUiFontFamily(value: UiFontFamily) {
        uiFontFamily.value = value
    }

    override fun changeUiFontScale(value: Float) {
        uiFontScale.value = value
    }

    override fun changeContentFontScale(value: Float) {
        contentFontScale.value = value
    }

    override fun changeNavItemTitles(value: Boolean) {
        navItemTitles.value = value
    }

    override fun changeDynamicColors(value: Boolean) {
        dynamicColors.value = value
    }

    override fun getCommentBarColor(
        depth: Int,
        maxDepth: Int,
        startColor: Color,
        endColor: Color,
    ): Color {
        if (depth == 0) {
            return Color.Transparent
        }
        val r1 = startColor.red
        val g1 = startColor.green
        val b1 = startColor.blue

        val r2 = endColor.red
        val g2 = endColor.green
        val b2 = endColor.blue

        val redStep = (r2 - r1) / maxDepth
        val greenStep = (g2 - g1) / maxDepth
        val blueStep = (b2 - b1) / maxDepth

        val index = ((depth - 1).coerceAtLeast(0) % maxDepth)
        return Color(
            r1 + redStep * index,
            g1 + greenStep * index,
            b1 + blueStep * index,
        )
    }

    override fun changeCustomSeedColor(color: Color?) {
        customSeedColor.value = color
    }

    override fun changeUpvoteColor(color: Color?) {
        upvoteColor.value = color
    }

    override fun changeDownvoteColor(color: Color?) {
        downvoteColor.value = color
    }

    override fun changePostLayout(value: PostLayout) {
        postLayout.value = value
    }
}
