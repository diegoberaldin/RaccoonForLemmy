package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

sealed interface ModeratorZoneAction {
    data object GlobalModLog : ModeratorZoneAction
    data object GlobalReports : ModeratorZoneAction

    data object ModeratedContents : ModeratorZoneAction

}

fun Int.toModeratorZoneAction(): ModeratorZoneAction = when (this) {
    2 -> ModeratorZoneAction.ModeratedContents
    1 -> ModeratorZoneAction.GlobalReports
    else -> ModeratorZoneAction.GlobalModLog
}

fun ModeratorZoneAction.toInt(): Int = when (this) {
    ModeratorZoneAction.GlobalModLog -> 0
    ModeratorZoneAction.GlobalReports -> 1
    ModeratorZoneAction.ModeratedContents -> 2
}

@Composable
fun ModeratorZoneAction.toReadableName(): String = when (this) {
    ModeratorZoneAction.GlobalModLog -> LocalXmlStrings.current.modlogTitle
    ModeratorZoneAction.GlobalReports -> LocalXmlStrings.current.reportListTitle
    ModeratorZoneAction.ModeratedContents -> LocalXmlStrings.current.moderatorZoneActionContents
}

@Composable
fun ModeratorZoneAction.toIcon(): ImageVector = when (this) {
    ModeratorZoneAction.GlobalModLog -> Icons.AutoMirrored.Default.ListAlt
    ModeratorZoneAction.GlobalReports -> Icons.Default.Report
    ModeratorZoneAction.ModeratedContents -> Icons.Default.Shield
}