package com.github.diegoberaldin.raccoonforlemmy.feature.search.di

import com.github.diegoberaldin.raccoonforlemmy.feature.search.content.ExporeViewModel
import org.koin.java.KoinJavaComponent.inject

actual fun getExploreViewModel(): ExporeViewModel {
    val res: ExporeViewModel by inject(ExporeViewModel::class.java)
    return res
}
