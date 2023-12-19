package com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.CommunityInfoMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getCommunityInfoViewModel(community: CommunityModel): CommunityInfoMviModel =
    UnitCommunityInfoDiHelper.getCommunityInfoModel(community)

object UnitCommunityInfoDiHelper : KoinComponent {
    fun getCommunityInfoModel(community: CommunityModel): CommunityInfoMviModel {
        val model: CommunityInfoMviModel by inject(
            parameters = { parametersOf(community) },
        )
        return model
    }
}