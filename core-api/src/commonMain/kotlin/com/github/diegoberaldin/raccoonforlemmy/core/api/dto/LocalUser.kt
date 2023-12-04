package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalUser(
    @SerialName("id") val id: LocalUserId,
    @SerialName("person_id") val personId: PersonId,
    @SerialName("email") val email: String? = null,
    @SerialName("show_nsfw") val showNsfw: Boolean? = null,
    @SerialName("theme") val theme: String? = null,
    @SerialName("default_sort_type") val defaultSortType: SortType? = null,
    @SerialName("default_listing_type") val defaultListingType: ListingType? = null,
    @SerialName("interface_language") val interfaceLanguage: String? = null,
    @SerialName("show_avatars") val showAvatars: Boolean? = null,
    @SerialName("send_notifications_to_email") val sendNotificationsToEmail: Boolean? = null,
    @SerialName("validator_time") val validatorTime: String? = null,
    @SerialName("show_scores") val showScores: Boolean? = null,
    @SerialName("show_bot_accounts") val showBotAccounts: Boolean? = null,
    @SerialName("show_read_posts") val showReadPosts: Boolean? = null,
    @SerialName("show_new_post_notifs") val showNewPostNotifs: Boolean? = null,
    @SerialName("email_verified") val emailVerified: Boolean? = null,
    @SerialName("accepted_application") val acceptedApplication: Boolean? = null,
    @SerialName("totp_2fa_url") val totp2faUrl: String? = null,
)
