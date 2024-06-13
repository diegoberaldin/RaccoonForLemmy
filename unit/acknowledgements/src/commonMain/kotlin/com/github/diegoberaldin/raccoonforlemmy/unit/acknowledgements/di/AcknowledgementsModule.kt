package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.di

import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.AcknowledgementsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.AcknowledgementsViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.datasource.AcknowledgementsRemoteDataSource
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.datasource.DefaultAcknowledgementsRemoteDataSource
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.repository.AcknowledgementsRepository
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.repository.DefaultAcknowledgementsRepository
import org.koin.dsl.module

val acknowledgementsModule =
    module {
        single<AcknowledgementsRemoteDataSource> {
            DefaultAcknowledgementsRemoteDataSource()
        }
        single<AcknowledgementsRepository> {
            DefaultAcknowledgementsRepository(
                dataSource = get(),
            )
        }
        factory<AcknowledgementsMviModel> {
            AcknowledgementsViewModel(
                acknowledgementsRepository = get(),
            )
        }
    }
