package com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.CommunityInfoMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getCommunityInfoViewModel(community: CommunityModel): CommunityInfoMviModel {
    val res: CommunityInfoMviModel by KoinJavaComponent.inject(
        clazz = CommunityInfoMviModel::class.java,
        parameters = { parametersOf(community) },
    )
    return res
}
