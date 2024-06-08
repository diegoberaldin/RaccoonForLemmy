package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings

sealed interface CommunityVisibilityType {
    data object Public : CommunityVisibilityType

    data object LocalOnly : CommunityVisibilityType
}

@Composable
fun CommunityVisibilityType.toIcon(): ImageVector =
    when (this) {
        CommunityVisibilityType.LocalOnly -> Icons.Default.Cottage
        else -> Icons.Default.Public
    }

@Composable
fun CommunityVisibilityType.toReadableName(): String =
    when (this) {
        CommunityVisibilityType.LocalOnly -> LocalStrings.current.communityVisibilityLocalOnly
        else -> LocalStrings.current.communityVisibilityPublic
    }
