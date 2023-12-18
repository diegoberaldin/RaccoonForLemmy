package com.github.diegoberaldin.raccoonforlemmy.unit.ban.di

import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf


actual fun getBanUserViewModel(
    userId: Int,
    communityId: Int,
    newValue: Boolean,
    postId: Int?,
    commentId: Int?,
): BanUserMviModel = BanDiHelper.getBanUserViewModel(
    userId,
    communityId,
    newValue,
    postId,
    commentId
)


object BanDiHelper : KoinComponent {
    fun getBanUserViewModel(
        userId: Int,
        communityId: Int,
        newValue: Boolean,
        postId: Int?,
        commentId: Int?,
    ): BanUserMviModel {
        val model: BanUserMviModel by inject(
            parameters = {
                parametersOf(
                    userId,
                    communityId,
                    newValue,
                    postId,
                    commentId,
                )
            }
        )
        return model
    }
}
