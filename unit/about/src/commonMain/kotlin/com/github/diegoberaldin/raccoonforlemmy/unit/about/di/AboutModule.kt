package com.github.diegoberaldin.raccoonforlemmy.unit.about.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutDialogMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutDialogViewModel
import org.koin.dsl.module

val aboutModule = module {
    factory<AboutDialogMviModel> {
        AboutDialogViewModel(
            mvi = DefaultMviModel(AboutDialogMviModel.UiState()),
        )
    }
}