package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaveUserSettingsForm(
    @SerialName("auth") val auth: String? = null,
    @SerialName("auto_expand") val autoExpand: Boolean? = null,
    @SerialName("avatar") val avatar: String? = null,
    @SerialName("banner") val banner: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("blur_nsfw") val blurNsfw: Boolean? = null,
    @SerialName("bot_account") val botAccount: Boolean? = null,
    @SerialName("collapse_bot_comments") val collapseBotComments: Boolean? = null,
    @SerialName("default_listing_type") val defaultListingType: ListingType? = null,
    @SerialName("default_sort_type") val defaultSortType: SortType? = null,
    @SerialName("discussion_languages") val discussionLanguages: List<LanguageId>? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("enable_animated_images") val enableAnimatedImages: Boolean? = null,
    @SerialName("enable_keyboard_navigation") val enableKeyboardNavigation: Boolean? = null,
    @SerialName("infinite_scroll_enabled") val infiniteScrollEnabled: Boolean? = null,
    @SerialName("interface_language") val interfaceLanguage: String? = null,
    @SerialName("matrix_user_id") val matrixUserId: String? = null,
    @SerialName("open_links_in_new_tab") val openLinksInNewTab: Boolean? = null,
    @SerialName("post_listing_mode") val postListingMode: PostListingMode? = null,
    @SerialName("send_notifications_to_email") val sendNotificationsToEmail: Boolean? = null,
    @SerialName("show_avatars") val showAvatars: Boolean? = null,
    @SerialName("show_bot_accounts") val showBotAccounts: Boolean? = null,
    @SerialName("show_nsfw") val showNsfw: Boolean? = null,
    @SerialName("show_read_posts") val showReadPosts: Boolean? = null,
    @SerialName("show_scores") val showScores: Boolean? = null,
    @SerialName("show_upvotes") val showUpvotes: Boolean? = null,
    @SerialName("show_downvotes") val showDownvotes: Boolean? = null,
    @SerialName("show_upvote_percentage") val showUpvotePercentage: Boolean? = null,
    @SerialName("theme") val theme: String? = null,
)
