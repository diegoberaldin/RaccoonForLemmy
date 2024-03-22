package com.github.diegoberaldin.raccoonforlemmy.unit.editcommunity.di

import com.github.diegoberaldin.raccoonforlemmy.unit.editcommunity.EditCommunityMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.editcommunity.EditCommunityViewModel
import org.koin.dsl.module

val editCommunityModule = module {
    factory<EditCommunityMviModel> { params ->
        EditCommunityViewModel(
            communityId = params[0],
            identityRepository = get(),
            communityRepository = get(),
            postRepository = get(),
        )
    }
}
