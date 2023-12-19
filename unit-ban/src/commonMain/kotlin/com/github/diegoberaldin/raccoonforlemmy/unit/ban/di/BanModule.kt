package com.github.diegoberaldin.raccoonforlemmy.unit.ban.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserViewModel
import org.koin.dsl.module

val banModule = module {
    factory<BanUserMviModel> { params ->
        BanUserViewModel(
            userId = params[0],
            communityId = params[1],
            newValue = params[2],
            postId = params[3],
            commentId = params[4],
            mvi = DefaultMviModel(BanUserMviModel.UiState()),
            identityRepository = get(),
            communityRepository = get(),
            notificationCenter = get(),
        )
    }
}