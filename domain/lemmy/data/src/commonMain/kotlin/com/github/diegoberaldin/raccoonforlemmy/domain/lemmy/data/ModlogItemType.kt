package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

sealed interface ModlogItemType {
    data object All : ModlogItemType
    data object ModRemovePost : ModlogItemType
    data object ModLockPost : ModlogItemType
    data object ModFeaturePost : ModlogItemType
    data object ModRemoveComment : ModlogItemType
    data object ModBanFromCommunity : ModlogItemType
    data object ModAdd : ModlogItemType
    data object ModTransferCommunity : ModlogItemType
}