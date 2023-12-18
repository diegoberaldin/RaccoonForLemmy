package com.github.diegoberaldin.raccoonforlemmy.unit.remove.di

import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf


actual fun getRemoveViewModel(
    postId: Int?,
    commentId: Int?,
): RemoveMviModel = UnitRemoveDiHelper.getRemoveModel(postId, commentId)

object UnitRemoveDiHelper : KoinComponent {
    fun getRemoveModel(
        postId: Int?,
        commentId: Int?,
    ): RemoveMviModel {
        val model: RemoveMviModel by inject(
            parameters = { parametersOf(postId, commentId) }
        )
        return model
    }
}
