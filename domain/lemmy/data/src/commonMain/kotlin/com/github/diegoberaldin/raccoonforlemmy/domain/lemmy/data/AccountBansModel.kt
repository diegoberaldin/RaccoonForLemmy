package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class AccountBansModel(
    val users: List<UserModel> = emptyList(),
    val communities: List<CommunityModel> = emptyList(),
    val instances: List<InstanceModel> = emptyList(),
) : JavaSerializable