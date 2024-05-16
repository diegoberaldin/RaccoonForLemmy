package com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.di

import com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.SelectInstanceMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.SelectInstanceViewModel
import org.koin.dsl.module

val selectInstanceModule =
    module {
        factory<SelectInstanceMviModel> {
            SelectInstanceViewModel(
                instanceRepository = get(),
                communityRepository = get(),
                apiConfigurationRepository = get(),
            )
        }
    }
