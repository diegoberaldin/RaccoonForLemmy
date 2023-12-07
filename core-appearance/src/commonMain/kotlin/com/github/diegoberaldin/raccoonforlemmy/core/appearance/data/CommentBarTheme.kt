package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
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
    else -> ""
}