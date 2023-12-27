package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class AccountSettingsModel(
    val avatar: String? = null,
    val banner: String? = null,
    val bio: String? = null,
    val bot: Boolean = false,
    val sendNotificationsToEmail: Boolean? = null,
    val displayName: String? = null,
    val matrixUserId: String? = null,
    val email: String? = null,
    val showBotAccounts: Boolean? = null,
    val showReadPosts: Boolean? = null,
    val showNsfw: Boolean? = null,
    val defaultListingType: ListingType? = null,
    val defaultSortType: SortType? = null,
) : JavaSerializable