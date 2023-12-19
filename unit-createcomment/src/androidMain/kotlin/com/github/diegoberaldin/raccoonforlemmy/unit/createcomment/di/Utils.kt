package com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.CreateCommentMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getCreateCommentViewModel(
    postId: Int?,
    parentId: Int?,
    editedCommentId: Int?,
): CreateCommentMviModel {
    val res: CreateCommentMviModel by KoinJavaComponent.inject(clazz = CreateCommentMviModel::class.java,
        parameters = { parametersOf(postId, parentId, editedCommentId) })
    return res
}

