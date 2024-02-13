package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

sealed interface CommentBarTheme {
    data object Green : CommentBarTheme
    data object Blue : CommentBarTheme
    data object Red : CommentBarTheme
    data object Rainbow : CommentBarTheme
}

fun Int?.toCommentBarTheme(): CommentBarTheme = when (this) {
    3 -> CommentBarTheme.Rainbow
    2 -> CommentBarTheme.Red
    1 -> CommentBarTheme.Green
    else -> CommentBarTheme.Blue
}

fun CommentBarTheme.toInt(): Int = when (this) {
    CommentBarTheme.Rainbow -> 3
    CommentBarTheme.Red -> 2
    CommentBarTheme.Green -> 1
    CommentBarTheme.Blue -> 0
}

@Composable
fun CommentBarTheme?.toReadableName() = when (this) {
    CommentBarTheme.Rainbow -> LocalXmlStrings.current.settingsCommentBarThemeMulti
    CommentBarTheme.Red -> LocalXmlStrings.current.settingsCommentBarThemeRed
    CommentBarTheme.Green -> LocalXmlStrings.current.settingsCommentBarThemeGreen
    CommentBarTheme.Blue -> LocalXmlStrings.current.settingsCommentBarThemeBlue
    else -> LocalXmlStrings.current.settingsColorCustom
}

fun CommentBarTheme?.toUpVoteColor(): Color = when (this) {
    CommentBarTheme.Rainbow -> Color(0xFF00FF00)
    CommentBarTheme.Red -> Color(0xFFD00000)
    CommentBarTheme.Green -> Color(0xFF52B788)
    CommentBarTheme.Blue -> Color(0xFF014F86)
    else -> Color.Transparent
}

fun CommentBarTheme?.toDownVoteColor(): Color = when (this) {
    CommentBarTheme.Rainbow -> Color(0xFFFF0000)
    CommentBarTheme.Red -> Color(0xFF2FFFFF)
    CommentBarTheme.Green -> Color(0xFFAD4877)
    CommentBarTheme.Blue -> Color(0xFF9400D3)
    else -> Color.Transparent
}

fun CommentBarTheme?.toReplyColor(): Color = when (this) {
    CommentBarTheme.Rainbow -> Color(0xFFFF5722)
    CommentBarTheme.Red -> Color(0xFF8BC34A)
    CommentBarTheme.Green -> Color(0xFFFF9800)
    CommentBarTheme.Blue -> Color(0xFF388E3C)
    else -> Color.Transparent
}

fun CommentBarTheme?.toSaveColor(): Color = when (this) {
    CommentBarTheme.Rainbow -> Color(0xFFE040FB)
    CommentBarTheme.Red -> Color(0xFFFFC107)
    CommentBarTheme.Green -> Color(0xFF388E3C)
    CommentBarTheme.Blue -> Color(0xFF7C4DFF)
    else -> Color.Transparent
}