package com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.di

import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.InstanceInfoMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getInstanceInfoViewModel(url: String): InstanceInfoMviModel =
    UnitInstanceInfoDiHelper.getInstanceInfoModel(url)


object UnitInstanceInfoDiHelper : KoinComponent {
    fun getInstanceInfoModel(url: String): InstanceInfoMviModel {
        val model: InstanceInfoMviModel by inject(
            parameters = { parametersOf(url) },
        )
        return model
    }
}
