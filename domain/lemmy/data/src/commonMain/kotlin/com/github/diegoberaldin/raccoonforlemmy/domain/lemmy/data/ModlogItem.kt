package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

sealed class ModlogItem(
    val type: ModlogItemType,
) : JavaSerializable {

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

    data class ModTransferCommunity(
        override val id: Int,
        override val date: String? = null,
        val user: UserModel? = null,
        val moderator: UserModel? = null,
    ) : ModlogItem(type = ModlogItemType.ModTransferCommunity)
}
