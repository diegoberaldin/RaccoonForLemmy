package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

sealed interface VoteFormat {
    data object Aggregated : VoteFormat
    data object Separated : VoteFormat
    data object Percentage : VoteFormat
}

fun VoteFormat.toLong(): Long = when (this) {
    VoteFormat.Percentage -> 2L
    VoteFormat.Separated -> 1L
    VoteFormat.Aggregated -> 0L
}

fun Long.toVoteFormat(): VoteFormat = when (this) {
    2L -> VoteFormat.Percentage
    1L -> VoteFormat.Separated
    else -> VoteFormat.Aggregated
}

@Composable
fun VoteFormat.toReadableName(): String = when (this) {
    VoteFormat.Aggregated -> stringResource(MR.strings.settings_vote_format_aggregated)
    VoteFormat.Percentage -> stringResource(MR.strings.settings_vote_format_percentage)
    VoteFormat.Separated -> stringResource(MR.strings.settings_vote_format_separated)
}

fun formatToReadableValue(
    voteFormat: VoteFormat,
    score: Int,
    upvotes: Int,
    downvotes: Int,
    upvoteColor: Color,
    downvoteColor: Color,
    upVoted: Boolean = false,
    downVoted: Boolean = false,
): AnnotatedString = buildAnnotatedString {
    when (voteFormat) {
        VoteFormat.Percentage -> {
            val totalVotes = upvotes + downvotes
            val percVote = if (totalVotes == 0) 0.0 else upvotes.toDouble() / totalVotes
            val text = "${(percVote * 100).toInt()} %"
            append(text)
            if (upVoted) {
                addStyle(
                    style = SpanStyle(color = upvoteColor),
                    start = 0,
                    end = text.length
                )
            } else if (downVoted) {
                addStyle(
                    style = SpanStyle(color = downvoteColor),
                    start = 0,
                    end = length
                )
            }
        }

        VoteFormat.Separated -> {
            val upvoteText = upvotes.toString()
            append(upvoteText)
            if (upVoted) {
                addStyle(
                    style = SpanStyle(color = upvoteColor),
                    start = 0,
                    end = upvoteText.length
                )
            }
            append(" / ")
            val downvoteText = downvotes.toString()
            append(downvoteText)
            if (downVoted) {
                addStyle(
                    style = SpanStyle(color = downvoteColor),
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
                    style = SpanStyle(color = upvoteColor),
                    start = 0,
                    end = text.length
                )
            } else if (downVoted) {
                addStyle(
                    style = SpanStyle(color = downvoteColor),
                    start = 0,
                    end = length
                )
            }
        }
    }
}