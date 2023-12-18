package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createreport.CreateReportMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.remove.RemoveMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.reportlist.ReportListMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.saveditems.SavedItemsMviModel
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

actual fun getCreateCommentViewModel(
    postId: Int?,
    parentId: Int?,
    editedCommentId: Int?,
): CreateCommentMviModel {
    val res: CreateCommentMviModel by inject(clazz = CreateCommentMviModel::class.java,
        parameters = { parametersOf(postId, parentId, editedCommentId) })
    return res
}

actual fun getCreatePostViewModel(editedPostId: Int?): CreatePostMviModel {
    val res: CreatePostMviModel by inject(clazz = CreatePostMviModel::class.java,
        parameters = { parametersOf(editedPostId) })
    return res
}
actual fun getSavedItemsViewModel(): SavedItemsMviModel {
    val res: SavedItemsMviModel by inject(
        clazz = SavedItemsMviModel::class.java,
    )
    return res
}

actual fun getCreateReportViewModel(
    postId: Int?,
    commentId: Int?,
): CreateReportMviModel {
    val res: CreateReportMviModel by inject(CreateReportMviModel::class.java, parameters = {
        parametersOf(postId, commentId)
    })
    return res
}

actual fun getRemoveViewModel(
    postId: Int?,
    commentId: Int?,
): RemoveMviModel {
    val res: RemoveMviModel by inject(RemoveMviModel::class.java, parameters = {
        parametersOf(postId, commentId)
    })
    return res
}

actual fun getReportListViewModel(
    communityId: Int,
): ReportListMviModel {
    val res: ReportListMviModel by inject(ReportListMviModel::class.java, parameters = {
        parametersOf(communityId)
    })
    return res
}

