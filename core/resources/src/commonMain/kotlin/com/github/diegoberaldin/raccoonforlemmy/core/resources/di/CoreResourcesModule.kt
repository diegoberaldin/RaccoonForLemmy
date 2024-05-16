package com.github.diegoberaldin.raccoonforlemmy.core.resources.di

import com.github.diegoberaldin.raccoonforlemmy.core.resources.CoreResources
import com.github.diegoberaldin.raccoonforlemmy.core.resources.DefaultCoreResources
import org.koin.dsl.module

val coreResourceModule =
    module {
        single<CoreResources> {
            DefaultCoreResources()
        }
    }
