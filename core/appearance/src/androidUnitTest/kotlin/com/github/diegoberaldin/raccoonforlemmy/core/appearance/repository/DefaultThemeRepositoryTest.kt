package com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository

import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.CommentBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class DefaultThemeRepositoryTest {

    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val sut = DefaultThemeRepository()

    @Test
    fun whenChangeUiTheme_thenValueIsAsExpected() {
        val resBefore = sut.uiTheme.value
        assertNull(resBefore)

        sut.changeUiTheme(UiTheme.Dark)

        val resAfter = sut.uiTheme.value
        assertEquals(UiTheme.Dark, resAfter)
    }

    @Test
    fun whenChangeUiFontFamily_thenValueIsAsExpected() {
        val resBefore = sut.uiFontFamily.value
        assertEquals(UiFontFamily.Poppins, resBefore)

        sut.changeUiFontFamily(UiFontFamily.NotoSans)

        val resAfter = sut.uiFontFamily.value
        assertEquals(UiFontFamily.NotoSans, resAfter)
    }

    @Test
    fun whenChangeContentFontScales_thenValueIsAsExpected() {
        val resBefore = sut.contentFontScale.value
        assertEquals(ContentFontScales(), resBefore)

        val value = ContentFontScales(title = 1.25f)
        sut.changeContentFontScale(value)

        val resAfter = sut.contentFontScale.value
        assertEquals(value, resAfter)
    }

    @Test
    fun whenChangeContentFontFamily_thenValueIsAsExpected() {
        val resBefore = sut.contentFontFamily.value
        assertEquals(UiFontFamily.Poppins, resBefore)

        sut.changeContentFontFamily(UiFontFamily.NotoSans)

        val resAfter = sut.contentFontFamily.value
        assertEquals(UiFontFamily.NotoSans, resAfter)
    }

    @Test
    fun whenChangeNavItemTitles_thenValueIsAsExpected() {
        val initial = sut.navItemTitles.value
        sut.changeNavItemTitles(!initial)

        val res = sut.navItemTitles.value
        assertNotEquals(initial, res)
    }

    @Test
    fun whenChangeDynamicColors_thenValueIsAsExpected() {
        val initial = sut.dynamicColors.value
        sut.changeDynamicColors(!initial)

        val res = sut.dynamicColors.value
        assertNotEquals(initial, res)
    }

    @Test
    fun whenChangePostLayout_thenValueIsAsExpected() {
        val resBefore = sut.postLayout.value
        assertEquals(PostLayout.Card, resBefore)

        sut.changePostLayout(PostLayout.Full)

        val resAfter = sut.postLayout.value
        assertEquals(PostLayout.Full, resAfter)
    }

    @Test
    fun whenChangeCustomSeedColor_thenValueIsAsExpected() {
        val resBefore = sut.customSeedColor.value
        assertNull(resBefore)

        sut.changeCustomSeedColor(Color.Red)

        val resAfter = sut.customSeedColor.value
        assertEquals(Color.Red, resAfter)
    }

    @Test
    fun whenChangeCommentBarTheme_thenValueIsAsExpected() {
        val resBefore = sut.commentBarTheme.value
        assertEquals(CommentBarTheme.Blue, resBefore)

        sut.changeCommentBarTheme(CommentBarTheme.Green)

        val resAfter = sut.commentBarTheme.value
        assertEquals(CommentBarTheme.Green, resAfter)
    }
}
