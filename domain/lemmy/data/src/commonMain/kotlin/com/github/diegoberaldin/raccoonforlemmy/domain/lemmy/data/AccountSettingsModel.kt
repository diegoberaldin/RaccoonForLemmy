package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

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
    val showScores: Boolean? = null,
    val defaultListingType: ListingType? = null,
    val defaultSortType: SortType? = null,
    val showUpVotes: Boolean? = null,
    val showDownVotes: Boolean? = null,
    val showUpVotePercentage: Boolean? = null,
)
