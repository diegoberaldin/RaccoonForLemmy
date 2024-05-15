package com.github.diegoberaldin.raccoonforlemmy.unit.licences.di

import com.github.diegoberaldin.raccoonforlemmy.unit.licences.LicencesMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.licences.LicencesViewModel
import org.koin.dsl.module

val licenceModule = module {
    factory<LicencesMviModel> { LicencesViewModel() }
}
