package com.github.diegoberaldin.raccoonforlemmy.di

import com.github.diegoberaldin.raccoonforlemmy.core.resources.CoreResources
import com.github.diegoberaldin.raccoonforlemmy.resources.SharedResources
import org.koin.dsl.module

internal val coreResourceModule =
    module {
        single<CoreResources> {
            SharedResources()
        }
    }
