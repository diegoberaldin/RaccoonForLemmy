package com.github.diegoberaldin.raccoonforlemmy.unit.modlog.di

import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.ModlogMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getModlogViewModel(communityId: Int): ModlogMviModel =
    UnitModlogDiHelper.getModlogViewModel(communityId)

object UnitModlogDiHelper : KoinComponent {
    fun getModlogViewModel(communityId: Int): ModlogMviModel {
        val res: ModlogMviModel by inject(parameters = { parametersOf(communityId) })
        return res
    }
}