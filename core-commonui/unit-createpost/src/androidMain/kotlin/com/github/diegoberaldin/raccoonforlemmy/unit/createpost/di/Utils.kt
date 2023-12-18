package com.github.diegoberaldin.raccoonforlemmy.unit.createpost.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getCreatePostViewModel(editedPostId: Int?): CreatePostMviModel {
    val res: CreatePostMviModel by KoinJavaComponent.inject(clazz = CreatePostMviModel::class.java,
        parameters = { parametersOf(editedPostId) })
    return res
}
