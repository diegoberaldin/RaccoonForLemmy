package com.github.diegoberaldin.raccoonforlemmy.unit.ban.di

import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserMviModel

expect fun getBanUserViewModel(
    userId: Int,
    communityId: Int,
    newValue: Boolean = true,
    postId: Int? = null,
    commentId: Int? = null,
): BanUserMviModel
