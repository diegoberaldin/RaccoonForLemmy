package com.github.diegoberaldin.raccoonforlemmy.unit.remove.di

import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getRemoveViewModel(
    postId: Int?,
    commentId: Int?,
): RemoveMviModel {
    val res: RemoveMviModel by KoinJavaComponent.inject(RemoveMviModel::class.java, parameters = {
        parametersOf(postId, commentId)
    })
    return res
}
