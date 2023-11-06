package com.github.diegoberaldin.raccoonforlemmy.core.utils.di

import com.github.diegoberaldin.raccoonforlemmy.core.utils.DefaultZombieModeHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ZombieModeHelper
import org.koin.dsl.module

val utilsModule = module {
    factory<ZombieModeHelper> {
        DefaultZombieModeHelper()
    }
}