package com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.CommunityDetailMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String,
): CommunityDetailMviModel =
    UnitCommunityDetailDiHelper.getCommunityDetailModel(community, otherInstance)

object UnitCommunityDetailDiHelper : KoinComponent {
    fun getCommunityDetailModel(
        community: CommunityModel,
        otherInstance: String,
    ): CommunityDetailMviModel {
        val model: CommunityDetailMviModel by inject(
            parameters = { parametersOf(community, otherInstance) },
        )
        return model
    }
}