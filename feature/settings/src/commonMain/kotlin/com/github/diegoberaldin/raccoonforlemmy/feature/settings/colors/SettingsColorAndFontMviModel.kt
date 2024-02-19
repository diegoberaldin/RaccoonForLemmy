package com.github.diegoberaldin.raccoonforlemmy.feature.settings.colors

import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.CommentBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontScales
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface SettingsColorAndFontMviModel :
    MviModel<SettingsColorAndFontMviModel.Intent, SettingsColorAndFontMviModel.UiState, SettingsColorAndFontMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class ChangeDynamicColors(val value: Boolean) : Intent
    }

    data class UiState(
        val isLogged: Boolean = false,
        val supportsDynamicColors: Boolean = false,
        val uiTheme: UiTheme? = null,
        val dynamicColors: Boolean = false,
        val customSeedColor: Color? = null,
        val upVoteColor: Color? = null,
        val downVoteColor: Color? = null,
        val replyColor: Color? = null,
        val saveColor: Color? = null,
        val commentBarTheme: CommentBarTheme = CommentBarTheme.Blue,
        val commentBarThickness: Int = 1,
        val uiFontScale: Float = 1f,
        val uiFontFamily: UiFontFamily = UiFontFamily.Poppins,
        val contentFontScale: ContentFontScales = ContentFontScales(),
        val contentFontFamily: UiFontFamily = UiFontFamily.Poppins,
    )

    sealed interface Effect
}