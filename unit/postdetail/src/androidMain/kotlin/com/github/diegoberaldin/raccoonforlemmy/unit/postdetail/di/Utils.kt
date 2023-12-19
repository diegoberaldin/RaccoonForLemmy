package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.PostDetailMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getPostDetailViewModel(
    post: PostModel,
    otherInstance: String,
    highlightCommentId: Int?,
    isModerator: Boolean,
): PostDetailMviModel {
    val res: PostDetailMviModel by KoinJavaComponent.inject(
        clazz = PostDetailMviModel::class.java,
        parameters = { parametersOf(post, otherInstance, highlightCommentId, isModerator) },
    )
    return res
}
