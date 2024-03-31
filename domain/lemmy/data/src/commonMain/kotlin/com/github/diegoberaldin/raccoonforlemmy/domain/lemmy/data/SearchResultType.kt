package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Padding
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

sealed interface SearchResultType {
    data object All : SearchResultType
    data object Posts : SearchResultType
    data object Comments : SearchResultType
    data object Users : SearchResultType
    data object Communities : SearchResultType
    data object Urls : SearchResultType
}


fun SearchResultType.toIcon(): ImageVector = when (this) {
    SearchResultType.All -> Icons.Default.AllInclusive
    SearchResultType.Comments -> Icons.AutoMirrored.Default.Message
    SearchResultType.Communities -> Icons.Default.Groups
    SearchResultType.Posts -> Icons.Default.Padding
    SearchResultType.Users -> Icons.Default.Person
    SearchResultType.Urls -> Icons.Default.AlternateEmail
}

@Composable
fun SearchResultType.toReadableName(): String = when (this) {
    SearchResultType.All -> LocalXmlStrings.current.exploreResultTypeAll
    SearchResultType.Comments -> LocalXmlStrings.current.exploreResultTypeComments
    SearchResultType.Communities -> LocalXmlStrings.current.exploreResultTypeCommunities
    SearchResultType.Posts -> LocalXmlStrings.current.exploreResultTypePosts
    SearchResultType.Users -> LocalXmlStrings.current.exploreResultTypeUsers
    SearchResultType.Urls -> LocalXmlStrings.current.createPostUrl
}
