package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

sealed interface ModlogItemType {
    data object All : ModlogItemType

    data object ModRemovePost : ModlogItemType

    data object ModLockPost : ModlogItemType

    data object ModFeaturePost : ModlogItemType

    data object ModRemoveComment : ModlogItemType

    data object ModBanFromCommunity : ModlogItemType

    data object ModAddCommunity : ModlogItemType

    data object ModTransferCommunity : ModlogItemType

    data object ModRemoveCommunity : ModlogItemType

    data object ModAdd : ModlogItemType

    data object ModBan : ModlogItemType

    data object ModHideCommunity : ModlogItemType

    data object AdminPurgePerson : ModlogItemType

    data object AdminPurgeCommunity : ModlogItemType

    data object AdminPurgePost : ModlogItemType

    data object AdminPurgeComment : ModlogItemType
}
