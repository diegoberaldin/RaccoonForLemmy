package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

sealed interface ActionOnSwipeDirection {
    data object ToStart : ActionOnSwipeDirection

    data object ToEnd : ActionOnSwipeDirection
}

sealed interface ActionOnSwipeTarget {
    data object Posts : ActionOnSwipeTarget

    data object Comments : ActionOnSwipeTarget

    data object Inbox : ActionOnSwipeTarget
}

sealed interface ActionOnSwipe {
    data object None : ActionOnSwipe

    data object UpVote : ActionOnSwipe

    data object DownVote : ActionOnSwipe

    data object Reply : ActionOnSwipe

    data object Save : ActionOnSwipe

    data object ToggleRead : ActionOnSwipe

    companion object {
        val DEFAULT_SWIPE_TO_START_POSTS =
            listOf(
                UpVote,
                Save,
            )

        val DEFAULT_SWIPE_TO_END_POSTS =
            listOf(
                DownVote,
                Reply,
            )

        val DEFAULT_SWIPE_TO_START_COMMENTS =
            listOf(
                UpVote,
                Save,
            )

        val DEFAULT_SWIPE_TO_END_COMMENTS =
            listOf(
                DownVote,
                Reply,
            )

        val DEFAULT_SWIPE_TO_START_INBOX =
            listOf(
                UpVote,
                ToggleRead,
            )

        val DEFAULT_SWIPE_TO_END_INBOX =
            listOf(
                DownVote,
            )
    }
}

internal fun ActionOnSwipe.toInt(): Int =
    when (this) {
        ActionOnSwipe.None -> 0
        ActionOnSwipe.UpVote -> 1
        ActionOnSwipe.DownVote -> 2
        ActionOnSwipe.Reply -> 3
        ActionOnSwipe.Save -> 4
        ActionOnSwipe.ToggleRead -> 5
    }

internal fun Int.toActionOnSwipe(): ActionOnSwipe =
    when (this) {
        1 -> ActionOnSwipe.UpVote
        2 -> ActionOnSwipe.DownVote
        3 -> ActionOnSwipe.Reply
        4 -> ActionOnSwipe.Save
        5 -> ActionOnSwipe.ToggleRead
        else -> ActionOnSwipe.None
    }

@Composable
fun ActionOnSwipe.toReadableName(): String =
    when (this) {
        ActionOnSwipe.DownVote -> LocalXmlStrings.current.actionDownvote
        ActionOnSwipe.None -> ""
        ActionOnSwipe.Reply -> LocalXmlStrings.current.actionReply
        ActionOnSwipe.Save -> LocalXmlStrings.current.actionSave
        ActionOnSwipe.ToggleRead -> LocalXmlStrings.current.actionToggleRead
        ActionOnSwipe.UpVote -> LocalXmlStrings.current.actionUpvote
    }

@Composable
fun ActionOnSwipe.toIcon(): ImageVector? =
    when (this) {
        ActionOnSwipe.DownVote -> Icons.Default.ArrowCircleDown
        ActionOnSwipe.None -> null
        ActionOnSwipe.Reply -> Icons.AutoMirrored.Default.Reply
        ActionOnSwipe.Save -> Icons.Default.Bookmark
        ActionOnSwipe.ToggleRead -> Icons.Default.MarkChatUnread
        ActionOnSwipe.UpVote -> Icons.Default.ArrowCircleUp
    }
