package com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.di

import com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.SelectCommunityMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getSelectCommunityViewModel(): SelectCommunityMviModel =
    UnitSelectCommunityDiHelper.selectCommunityViewModel

object UnitSelectCommunityDiHelper : KoinComponent {
    val selectCommunityViewModel: SelectCommunityMviModel by inject()
}