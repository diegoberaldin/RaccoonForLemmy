package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName

enum class ModlogActionType {
    @SerialName("All")
    All,

    @SerialName("ModRemovePost")
    ModRemovePost,

    @SerialName("ModLockPost")
    ModLockPost,

    @SerialName("ModFeaturePost")
    ModFeaturePost,

    @SerialName("ModRemoveComment")
    ModRemoveComment,

    @SerialName("ModRemoveCommunity")
    ModRemoveCommunity,

    @SerialName("ModBanFromCommunity")
    ModBanFromCommunity,

    @SerialName("ModAddCommunity")
    ModAddCommunity,

    @SerialName("ModTransferCommunity")
    ModTransferCommunity,

    @SerialName("ModAdd")
    ModAdd,

    @SerialName("ModBan")
    ModBan,

    @SerialName("ModHideCommunity")
    ModHideCommunity,

    @SerialName("AdminPurgePerson")
    AdminPurgePerson,

    @SerialName("AdminPurgeCommunity")
    AdminPurgeCommunity,

    @SerialName("AdminPurgePost")
    AdminPurgePost,

    @SerialName("AdminPurgeComment")
    AdminPurgeComment,
}
