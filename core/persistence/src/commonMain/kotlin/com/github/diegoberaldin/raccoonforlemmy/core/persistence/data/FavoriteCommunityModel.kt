package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class FavoriteCommunityModel(
    val id: Long? = null,
    val communityId: Int? = null,
) : JavaSerializable