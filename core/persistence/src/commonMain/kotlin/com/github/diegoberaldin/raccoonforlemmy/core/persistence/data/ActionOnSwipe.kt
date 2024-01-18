package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data

sealed interface ActionOnSwipe {
    data object None : ActionOnSwipe
    data object UpVote : ActionOnSwipe
    data object DownVote : ActionOnSwipe
    data object Reply : ActionOnSwipe
    data object Save : ActionOnSwipe
    data object ToggleRead : ActionOnSwipe
}

internal fun ActionOnSwipe.toInt(): Int = when (this) {
    ActionOnSwipe.None -> 0
    ActionOnSwipe.UpVote -> 1
    ActionOnSwipe.DownVote -> 2
    ActionOnSwipe.Reply -> 3
    ActionOnSwipe.Save -> 4
    ActionOnSwipe.ToggleRead -> 5
}

internal fun Int.toActionOnSwipe(): ActionOnSwipe = when (this) {
    1 -> ActionOnSwipe.UpVote
    2 -> ActionOnSwipe.DownVote
    3 -> ActionOnSwipe.Reply
    4 -> ActionOnSwipe.Save
    5 -> ActionOnSwipe.ToggleRead
    else -> ActionOnSwipe.None
}