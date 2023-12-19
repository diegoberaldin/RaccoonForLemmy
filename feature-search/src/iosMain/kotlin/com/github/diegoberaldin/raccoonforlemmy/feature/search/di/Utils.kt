package com.github.diegoberaldin.raccoonforlemmy.feature.search.di

import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getExploreViewModel(): ExploreMviModel = ExploreInjectHelper.model

object ExploreInjectHelper : KoinComponent {
    val model: ExploreMviModel by inject()
}
