package com.github.diegoberaldin.raccoonforlemmy.feature.search.di

import com.github.diegoberaldin.raccoonforlemmy.unit.explore.di.exploreModule
import org.koin.dsl.module

val searchTabModule = module {
    includes(exploreModule)
}
