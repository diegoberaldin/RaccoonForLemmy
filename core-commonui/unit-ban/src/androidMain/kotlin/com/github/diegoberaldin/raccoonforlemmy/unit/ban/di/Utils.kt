package com.github.diegoberaldin.raccoonforlemmy.unit.ban.di

import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getBanUserViewModel(
    userId: Int,
    communityId: Int,
    newValue: Boolean,
    postId: Int?,
    commentId: Int?,
): BanUserMviModel {
    val res: BanUserMviModel by KoinJavaComponent.inject(BanUserMviModel::class.java, parameters = {
        parametersOf(
            userId,
            communityId,
            newValue,
            postId,
            commentId,
        )
    })
    return res
}
