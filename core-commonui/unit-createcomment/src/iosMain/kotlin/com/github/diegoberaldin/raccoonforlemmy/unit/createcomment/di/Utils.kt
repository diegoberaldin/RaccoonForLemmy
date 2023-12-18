package com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.CreateCommentMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getCreateCommentViewModel(
    postId: Int?,
    parentId: Int?,
    editedCommentId: Int?,
): CreateCommentMviModel =
    UnitCreateCommentDiHelper.getCreateCommentModel(postId, parentId, editedCommentId)

object UnitCreateCommentDiHelper : KoinComponent {
    fun getCreateCommentModel(
        postId: Int?,
        parentId: Int?,
        editedCommentId: Int?,
    ): CreateCommentMviModel {
        val model: CreateCommentMviModel by inject(
            parameters = { parametersOf(postId, parentId, editedCommentId) }
        )
        return model
    }
}