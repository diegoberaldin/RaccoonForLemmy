package com.github.diegoberaldin.raccoonforlemmy.core.l10n.di

import com.github.diegoberaldin.raccoonforlemmy.core.l10n.DefaultL10nManager
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.L10nManager
import org.koin.dsl.module

val coreL10nModule =
    module {
        single<L10nManager> {
            DefaultL10nManager()
        }
    }
