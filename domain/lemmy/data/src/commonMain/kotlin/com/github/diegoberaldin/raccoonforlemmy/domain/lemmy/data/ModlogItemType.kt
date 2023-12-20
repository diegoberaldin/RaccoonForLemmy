package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

sealed interface ModlogItemType : JavaSerializable {
    data object All : ModlogItemType
    data object ModRemovePost : ModlogItemType
    data object ModLockPost : ModlogItemType
    data object ModFeaturePost : ModlogItemType
    data object ModRemoveComment : ModlogItemType
    data object ModBanFromCommunity : ModlogItemType
    data object ModAdd : ModlogItemType
    data object ModTransferCommunity : ModlogItemType
}