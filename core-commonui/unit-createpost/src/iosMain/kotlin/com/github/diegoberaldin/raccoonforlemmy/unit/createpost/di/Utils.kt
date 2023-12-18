package com.github.diegoberaldin.raccoonforlemmy.unit.createpost.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getCreatePostViewModel(
    editedPostId: Int?,
): CreatePostMviModel =
    UnitCreatePostDiHelper.getCreatePostModel(editedPostId)

object UnitCreatePostDiHelper : KoinComponent {
    fun getCreatePostModel(editedPostId: Int?): CreatePostMviModel {
        val model: CreatePostMviModel by inject(
            parameters = { parametersOf(editedPostId) }
        )
        return model
    }
}