package com.github.diegoberaldin.raccoonforlemmy.unit.postlist.di

import com.github.diegoberaldin.raccoonforlemmy.unit.postlist.PostListMviModel
import org.koin.java.KoinJavaComponent.inject

actual fun getHomeScreenModel(): PostListMviModel {
    val res: PostListMviModel by inject(PostListMviModel::class.java)
    return res
}