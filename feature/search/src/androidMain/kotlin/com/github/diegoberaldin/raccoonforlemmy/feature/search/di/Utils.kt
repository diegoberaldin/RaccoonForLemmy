package com.github.diegoberaldin.raccoonforlemmy.feature.search.di

import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreMviModel
import org.koin.java.KoinJavaComponent.inject

actual fun getExploreViewModel(): ExploreMviModel {
    val res: ExploreMviModel by inject(ExploreMviModel::class.java)
    return res
}
