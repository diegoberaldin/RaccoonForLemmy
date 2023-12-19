package com.github.diegoberaldin.raccoonforlemmy.unit.createreport.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportViewModel
import org.koin.dsl.module

val createReportModule = module {
    factory<CreateReportMviModel> { params ->
        CreateReportViewModel(
            postId = params[0],
            commentId = params[1],
            mvi = DefaultMviModel(CreateReportMviModel.UiState()),
            identityRepository = get(),
            postRepository = get(),
            commentRepository = get(),
        )
    }
}