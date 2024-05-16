package com.github.diegoberaldin.raccoonforlemmy.core.utils.di

import com.github.diegoberaldin.raccoonforlemmy.core.utils.zombiemode.DefaultZombieModeHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.zombiemode.ZombieModeHelper
import org.koin.dsl.module

val utilsModule =
    module {
        factory<ZombieModeHelper> {
            DefaultZombieModeHelper()
        }
    }
