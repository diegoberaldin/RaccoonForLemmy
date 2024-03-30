package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

sealed interface VoteFormat {
    data object Aggregated : VoteFormat
    data object Separated : VoteFormat
    data object Percentage : VoteFormat
    data object Hidden : VoteFormat
}

fun VoteFormat.toLong(): Long = when (this) {
    VoteFormat.Percentage -> 2L
    VoteFormat.Separated -> 1L
    VoteFormat.Aggregated -> 0L
    VoteFormat.Hidden -> -1L
}

fun Long.toVoteFormat(): VoteFormat = when (this) {
    2L -> VoteFormat.Percentage
    1L -> VoteFormat.Separated
    -1L -> VoteFormat.Hidden
    else -> VoteFormat.Aggregated
}

@Composable
fun VoteFormat.toReadableName(): String = when (this) {
    VoteFormat.Percentage -> LocalXmlStrings.current.settingsVoteFormatPercentage
    VoteFormat.Separated -> LocalXmlStrings.current.settingsVoteFormatSeparated
    VoteFormat.Hidden -> LocalXmlStrings.current.settingsVoteFormatHidden
    else -> LocalXmlStrings.current.settingsVoteFormatAggregated
}

fun formatToReadableValue(
    voteFormat: VoteFormat,
    score: Int,
    upVotes: Int,
    downVotes: Int,
    upVoteColor: Color,
    downVoteColor: Color,
    upVoted: Boolean = false,
    downVoted: Boolean = false,
): AnnotatedString = buildAnnotatedString {
    when (voteFormat) {
        VoteFormat.Percentage -> {
            val totalVotes = upVotes + downVotes
            val percVote = if (totalVotes == 0) 0.0 else upVotes.toDouble() / totalVotes
            val text = "${(percVote * 100).toInt()} %"
            append(text)
            if (upVoted) {
                addStyle(
                    style = SpanStyle(color = upVoteColor),
                    start = 0,
                    end = text.length
                )
            } else if (downVoted) {
                addStyle(
                    style = SpanStyle(color = downVoteColor),
                    start = 0,
                    end = length
                )
            }
        }

        VoteFormat.Separated -> {
            val upvoteText = upVotes.toString()
            append(upvoteText)
            if (upVoted) {
                addStyle(
                    style = SpanStyle(color = upVoteColor),
                    start = 0,
                    end = upvoteText.length
                )
            }
            append(" / ")
            val downvoteText = downVotes.toString()
            append(downvoteText)
            if (downVoted) {
                addStyle(
                    style = SpanStyle(color = downVoteColor),
                    start = upvoteText.length + 3,
                    end = upvoteText.length + 3 + downvoteText.length
                )
            }
        }

        else -> {
            val text = score.toString()
            append(text)
            if (upVoted) {
                addStyle(
                    style = SpanStyle(color = upVoteColor),
                    start = 0,
                    end = text.length
                )
            } else if (downVoted) {
                addStyle(
                    style = SpanStyle(color = downVoteColor),
                    start = 0,
                    end = length
                )
            }
        }
    }
}
