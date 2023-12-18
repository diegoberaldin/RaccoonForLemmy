package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createreport.CreateReportMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.reportlist.ReportListMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.saveditems.SavedItemsMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getPostDetailViewModel(
    post: PostModel,
    otherInstance: String,
    highlightCommentId: Int?,
    isModerator: Boolean,
): PostDetailMviModel =
    CommonUiViewModelHelper.getPostDetailModel(post, otherInstance, highlightCommentId, isModerator)

actual fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String,
): CommunityDetailMviModel =
    CommonUiViewModelHelper.getCommunityDetailModel(community, otherInstance)

actual fun getUserDetailViewModel(user: UserModel, otherInstance: String): UserDetailMviModel =
    CommonUiViewModelHelper.getUserDetailModel(user, otherInstance)

actual fun getCreateCommentViewModel(
    postId: Int?,
    parentId: Int?,
    editedCommentId: Int?,
): CreateCommentMviModel =
    CommonUiViewModelHelper.getCreateCommentModel(postId, parentId, editedCommentId)

actual fun getCreatePostViewModel(
    editedPostId: Int?,
): CreatePostMviModel =
    CommonUiViewModelHelper.getCreatePostModel(editedPostId)

actual fun getSavedItemsViewModel(): SavedItemsMviModel =
    CommonUiViewModelHelper.savedItemsViewModel

actual fun getCreateReportViewModel(
    postId: Int?,
    commentId: Int?,
): CreateReportMviModel = CommonUiViewModelHelper.getCreateReportModel(postId, commentId)

actual fun getReportListViewModel(
    communityId: Int,
): ReportListMviModel = CommonUiViewModelHelper.getReportListViewModel(communityId)

object CommonUiViewModelHelper : KoinComponent {

    val savedItemsViewModel: SavedItemsMviModel by inject()

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

    fun getCommunityDetailModel(
        community: CommunityModel,
        otherInstance: String,
    ): CommunityDetailMviModel {
        val model: CommunityDetailMviModel by inject(
            parameters = { parametersOf(community, otherInstance) },
        )
        return model
    }

    fun getUserDetailModel(user: UserModel, otherInstance: String): UserDetailMviModel {
        val model: UserDetailMviModel by inject(
            parameters = { parametersOf(user, otherInstance) },
        )
        return model
    }

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

    fun getCreatePostModel(editedPostId: Int?): CreatePostMviModel {
        val model: CreatePostMviModel by inject(
            parameters = { parametersOf(editedPostId) }
        )
        return model
    }

    fun getCreateReportModel(
        postId: Int?,
        commentId: Int?,
    ): CreateReportMviModel {
        val model: CreateReportMviModel by inject(
            parameters = { parametersOf(postId, commentId) }
        )
        return model
    }

    fun getReportListViewModel(
        communityId: Int,
    ): ReportListMviModel {
        val model: ReportListMviModel by inject(
            parameters = { parametersOf(communityId) }
        )
        return model
    }
}
