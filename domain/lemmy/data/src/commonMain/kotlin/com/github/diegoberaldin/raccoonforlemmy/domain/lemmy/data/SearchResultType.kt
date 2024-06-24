package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings

sealed interface SearchResultType {
    data object All : SearchResultType

    data object Posts : SearchResultType

    data object Comments : SearchResultType

    data object Users : SearchResultType

    data object Communities : SearchResultType

    data object Urls : SearchResultType
}

fun SearchResultType.toIcon(): ImageVector =
    when (this) {
        SearchResultType.All -> Icons.Default.AllInclusive
        SearchResultType.Comments -> Icons.AutoMirrored.Default.Message
        SearchResultType.Communities -> Icons.Default.Groups
        SearchResultType.Posts -> Icons.AutoMirrored.Default.Article
        SearchResultType.Users -> Icons.Default.Person
        SearchResultType.Urls -> Icons.Default.AlternateEmail
    }

@Composable
fun SearchResultType.toReadableName(): String =
    when (this) {
        SearchResultType.All -> LocalStrings.current.exploreResultTypeAll
        SearchResultType.Comments -> LocalStrings.current.exploreResultTypeComments
        SearchResultType.Communities -> LocalStrings.current.exploreResultTypeCommunities
        SearchResultType.Posts -> LocalStrings.current.exploreResultTypePosts
        SearchResultType.Users -> LocalStrings.current.exploreResultTypeUsers
        SearchResultType.Urls -> LocalStrings.current.createPostUrl
    }

fun SearchResultType.toInt(): Int =
    when (this) {
        SearchResultType.Posts -> 1
        SearchResultType.Communities -> 2
        SearchResultType.Users -> 3
        SearchResultType.Comments -> 4
        SearchResultType.Urls -> 5
        else -> 0
    }

fun Int.toSearchResultType(): SearchResultType =
    when (this) {
        1 -> SearchResultType.Posts
        2 -> SearchResultType.Communities
        3 -> SearchResultType.Users
        4 -> SearchResultType.Comments
        5 -> SearchResultType.Urls
        else -> SearchResultType.All
    }
