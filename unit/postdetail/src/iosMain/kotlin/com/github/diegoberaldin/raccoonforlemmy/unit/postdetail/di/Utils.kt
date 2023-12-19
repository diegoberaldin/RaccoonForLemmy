package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.PostDetailMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getPostDetailViewModel(
    post: PostModel,
    otherInstance: String,
    highlightCommentId: Int?,
    isModerator: Boolean,
): PostDetailMviModel =
    UnitPostDetailDiHelper.getPostDetailModel(post, otherInstance, highlightCommentId, isModerator)

object UnitPostDetailDiHelper : KoinComponent {
    fun getPostDetailModel(
        post: PostModel,
        otherInstance: String,
        highlightCommentId: Int?,
        isModerator: Boolean,
    ): PostDetailMviModel {
        val model: PostDetailMviModel by inject(
            parameters = { parametersOf(post, otherInstance, highlightCommentId, isModerator) },
        )
        return model
    }
}
