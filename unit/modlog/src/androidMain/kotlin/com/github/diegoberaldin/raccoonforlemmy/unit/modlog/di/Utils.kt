package com.github.diegoberaldin.raccoonforlemmy.unit.modlog.di

import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.ModlogMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject

actual fun getModlogViewModel(communityId: Int): ModlogMviModel {
    val res: ModlogMviModel by inject(
        clazz = ModlogMviModel::class.java,
        parameters = {
            parametersOf(communityId)
        },
    )
    return res
}