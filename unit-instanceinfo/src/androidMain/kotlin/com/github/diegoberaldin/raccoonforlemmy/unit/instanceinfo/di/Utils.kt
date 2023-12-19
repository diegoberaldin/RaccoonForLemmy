package com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.di

import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.InstanceInfoMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getInstanceInfoViewModel(url: String): InstanceInfoMviModel {
    val res: InstanceInfoMviModel by KoinJavaComponent.inject(
        clazz = InstanceInfoMviModel::class.java,
        parameters = { parametersOf(url) },
    )
    return res
}
