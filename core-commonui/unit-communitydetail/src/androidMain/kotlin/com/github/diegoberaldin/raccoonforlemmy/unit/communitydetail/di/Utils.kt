package com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.CommunityDetailMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String,
): CommunityDetailMviModel {
    val res: CommunityDetailMviModel by KoinJavaComponent.inject(
        clazz = CommunityDetailMviModel::class.java,
        parameters = { parametersOf(community, otherInstance) },
    )
    return res
}
