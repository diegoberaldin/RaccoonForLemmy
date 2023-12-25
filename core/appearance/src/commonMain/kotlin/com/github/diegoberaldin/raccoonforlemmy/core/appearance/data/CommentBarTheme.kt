package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

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
    CommentBarTheme.Rainbow -> stringResource(MR.strings.settings_comment_bar_theme_multi)
    CommentBarTheme.Red -> stringResource(MR.strings.settings_comment_bar_theme_red)
    CommentBarTheme.Green -> stringResource(MR.strings.settings_comment_bar_theme_green)
    CommentBarTheme.Blue -> stringResource(MR.strings.settings_comment_bar_theme_blue)
    else -> stringResource(MR.strings.settings_color_custom)
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