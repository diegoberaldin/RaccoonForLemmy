package com.github.diegoberaldin.raccoonforlemmy.core.appearance.di

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.DefaultThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreAppearanceModule =
    module {
        includes(nativeAppearanceModule)

        singleOf<ThemeRepository>(::DefaultThemeRepository)
    }
