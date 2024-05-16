package com.github.diegoberaldin.raccoonforlemmy.feature.home.di

import com.github.diegoberaldin.raccoonforlemmy.unit.postlist.di.postListModule
import org.koin.dsl.module

val homeTabModule =
    module {
        includes(
            postListModule,
        )
    }
