package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetModlogResponse(
    @SerialName("added") val added: List<ModAddView>? = null,
    @SerialName("added_to_community") val addedToCommunity: List<ModAddCommunityView>? = null,
    @SerialName("admin_purged_comments") val adminPurgedComments: List<AdminPurgeCommentView>? = null,
    @SerialName("admin_purged_communities") val adminPurgedCommunities: List<AdminPurgeCommunityView>? = null,
    @SerialName("admin_purged_persons") val adminPurgedPersons: List<AdminPurgePersonView>? = null,
    @SerialName("admin_purged_posts") val adminPurgedPosts: List<AdminPurgePostView>? = null,
    @SerialName("banned") val banned: List<ModBanView>? = null,
    @SerialName("banned_from_community") val bannedFromCommunity: List<ModBanFromCommunityView>? = null,
    @SerialName("featured_posts") val featuredPosts: List<ModFeaturePostView>? = null,
    @SerialName("hidden_communities") val hiddenCommunities: List<ModHideCommunityView>? = null,
    @SerialName("locked_posts") val lockedPosts: List<ModLockPostView>? = null,
    @SerialName("removed_comments") val removedComments: List<ModRemoveCommentView>? = null,
    @SerialName("removed_communities") val removedCommunities: List<ModRemoveCommunityView>? = null,
    @SerialName("removed_posts") val removedPosts: List<ModRemovePostView>? = null,
    @SerialName("transferred_to_community") val transferredToCommunity: List<ModTransferCommunityView>? = null,
)
