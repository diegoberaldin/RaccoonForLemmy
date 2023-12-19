package com.github.diegoberaldin.raccoonforlemmy.unit.postlist.di

import com.github.diegoberaldin.raccoonforlemmy.unit.postlist.PostListMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getHomeScreenModel(): PostListMviModel = UnitPostListDiHelper.model

object UnitPostListDiHelper : KoinComponent {
    val model: PostListMviModel by inject()
}
