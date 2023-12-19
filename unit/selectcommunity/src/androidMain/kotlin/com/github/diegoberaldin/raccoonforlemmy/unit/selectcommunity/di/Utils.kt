package com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.di

import com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.SelectCommunityMviModel
import org.koin.java.KoinJavaComponent

actual fun getSelectCommunityViewModel(): SelectCommunityMviModel {
    val res: SelectCommunityMviModel by KoinJavaComponent.inject(SelectCommunityMviModel::class.java)
    return res
}
