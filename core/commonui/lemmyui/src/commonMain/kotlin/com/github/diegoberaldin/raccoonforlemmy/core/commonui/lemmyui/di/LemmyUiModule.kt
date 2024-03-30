package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.DefaultFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.FabNestedScrollConnection
import org.koin.dsl.module

val lemmyUiModule = module {
    factory<FabNestedScrollConnection> {
        DefaultFabNestedScrollConnection()
    }
}
