package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject

actual fun getPostDetailViewModel(
    post: PostModel,
    otherInstance: String,
    highlightCommentId: Int?,
    isModerator: Boolean,
): PostDetailMviModel {
    val res: PostDetailMviModel by inject(
        clazz = PostDetailMviModel::class.java,
        parameters = { parametersOf(post, otherInstance, highlightCommentId, isModerator) },
    )
    return res
}

actual fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String,
): CommunityDetailMviModel {
    val res: CommunityDetailMviModel by inject(
        clazz = CommunityDetailMviModel::class.java,
        parameters = { parametersOf(community, otherInstance) },
    )
    return res
}

actual fun getUserDetailViewModel(user: UserModel, otherInstance: String): UserDetailMviModel {
    val res: UserDetailMviModel by inject(
        clazz = UserDetailMviModel::class.java,
        parameters = { parametersOf(user, otherInstance) },
    )
    return res
}
