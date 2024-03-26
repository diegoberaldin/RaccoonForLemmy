package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

sealed class ModlogItem(
    val type: ModlogItemType,
) {

    abstract val id: Int
    abstract val date: String?

    data class ModRemovePost(
        override val id: Int,
        override val date: String? = null,
        val removed: Boolean = false,
        val user: UserModel? = null,
        val moderator: UserModel? = null,
        val reason: String? = null,
        val post: PostModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModRemovePost)

    data class ModLockPost(
        override val id: Int,
        override val date: String? = null,
        val locked: Boolean = false,
        val moderator: UserModel? = null,
        val post: PostModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModLockPost)

    data class ModFeaturePost(
        override val id: Int,
        override val date: String? = null,
        val featured: Boolean = false,
        val moderator: UserModel? = null,
        val post: PostModel? = null,
    ) :
        ModlogItem(type = ModlogItemType.ModFeaturePost)

    data class ModRemoveComment(
        override val id: Int,
        override val date: String? = null,
        val removed: Boolean = false,
        val user: UserModel? = null,
        val moderator: UserModel? = null,
        val reason: String? = null,
        val comment: CommentModel? = null,
        val post: PostModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModRemoveComment)

    data class ModBanFromCommunity(
        override val id: Int,
        override val date: String? = null,
        val banned: Boolean = false,
        val user: UserModel? = null,
        val moderator: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModBanFromCommunity)

    data class ModAdd(
        override val id: Int,
        override val date: String? = null,
        val removed: Boolean = false,
        val user: UserModel? = null,
        val moderator: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModAdd)

    data class ModAddCommunity(
        override val id: Int,
        override val date: String? = null,
        val removed: Boolean = false,
        val user: UserModel? = null,
        val moderator: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModAddCommunity)

    data class AdminPurgeCommunity(
        override val id: Int,
        override val date: String? = null,
        val admin: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.AdminPurgeCommunity)

    data class AdminPurgeComment(
        override val id: Int,
        override val date: String? = null,
        val post: PostModel? = null,
        val admin: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.AdminPurgeComment)

    data class AdminPurgePerson(
        override val id: Int,
        override val date: String? = null,
        val admin: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.AdminPurgePerson)

    data class AdminPurgePost(
        override val id: Int,
        override val date: String? = null,
        val admin: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.AdminPurgePost)

    data class ModBan(
        override val id: Int,
        override val date: String? = null,
        val banned: Boolean = false,
        val user: UserModel? = null,
        val moderator: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModBan)

    data class HideCommunity(
        override val id: Int,
        override val date: String? = null,
        val hidden: Boolean = false,
        val community: CommunityModel? = null,
        val admin: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModHideCommunity)

    data class RemoveCommunity(
        override val id: Int,
        override val date: String? = null,
        val community: CommunityModel? = null,
        val moderator: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModRemoveCommunity)

    data class ModTransferCommunity(
        override val id: Int,
        override val date: String? = null,
        val user: UserModel? = null,
        val moderator: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModTransferCommunity)
}
