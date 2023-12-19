package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

sealed interface ListingType : JavaSerializable {
    data object All : ListingType
    data object Subscribed : ListingType
    data object Local : ListingType
}

fun ListingType.toInt() = when (this) {
    ListingType.All -> 2
    ListingType.Subscribed -> 1
    else -> 0
}

fun Int.toListingType() = when (this) {
    1 -> ListingType.Subscribed
    2 -> ListingType.All
    else -> ListingType.Local
}

fun ListingType.toIcon() = when (this) {
    ListingType.Local -> Icons.Default.Cottage
    ListingType.Subscribed -> Icons.Default.Book
    else -> Icons.Default.Public
}

@Composable
fun ListingType.toReadableName(): String = when (this) {
    ListingType.All -> stringResource(MR.strings.home_listing_type_all)
    ListingType.Local -> stringResource(MR.strings.home_listing_type_local)
    ListingType.Subscribed -> stringResource(MR.strings.home_listing_type_subscribed)
}
