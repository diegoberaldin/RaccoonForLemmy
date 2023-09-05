package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.comments.UserCommentsViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.posts.UserPostsViewModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getPostDetailViewModel(post: PostModel): PostDetailViewModel =
    CommonUiViewModelHelper.getPostDetailModel(post)

actual fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String,
): CommunityDetailViewModel =
    CommonUiViewModelHelper.getCommunityDetailModel(community, otherInstance)

actual fun getCommunityInfoViewModel(community: CommunityModel): CommunityInfoViewModel =
    CommonUiViewModelHelper.getCommunityInfoModel(community)

actual fun getInstanceInfoViewModel(url: String): InstanceInfoViewModel =
    CommonUiViewModelHelper.getInstanceInfoModel(url)

actual fun getUserDetailViewModel(user: UserModel): UserDetailViewModel =
    CommonUiViewModelHelper.getUserDetailModel(user)

actual fun getUserPostsViewModel(user: UserModel): UserPostsViewModel =
    CommonUiViewModelHelper.getUserPostsModel(user)

actual fun getUserCommentsViewModel(user: UserModel): UserCommentsViewModel =
    CommonUiViewModelHelper.getUserCommentsModel(user)

actual fun getCreateCommentViewModel(postId: Int, parentId: Int?): CreateCommentViewModel =
    CommonUiViewModelHelper.getCreateCommentModel(postId, parentId)

actual fun getCreatePostViewModel(communityId: Int): CreatePostViewModel =
    CommonUiViewModelHelper.getCreatePostModel(communityId)

object CommonUiViewModelHelper : KoinComponent {

    fun getPostDetailModel(post: PostModel): PostDetailViewModel {
        val model: PostDetailViewModel by inject(
            parameters = { parametersOf(post) },
        )
        return model
    }

    fun getCommunityDetailModel(
        community: CommunityModel,
        otherInstance: String,
    ): CommunityDetailViewModel {
        val model: CommunityDetailViewModel by inject(
            parameters = { parametersOf(community, otherInstance) },
        )
        return model
    }

    fun getCommunityInfoModel(community: CommunityModel): CommunityInfoViewModel {
        val model: CommunityInfoViewModel by inject(
            parameters = { parametersOf(community) },
        )
        return model
    }

    fun getInstanceInfoModel(url: String): InstanceInfoViewModel {
        val model: InstanceInfoViewModel by inject(
            parameters = { parametersOf(url) },
        )
        return model
    }

    fun getUserDetailModel(user: UserModel): UserDetailViewModel {
        val model: UserDetailViewModel by inject(
            parameters = { parametersOf(user) },
        )
        return model
    }

    fun getUserPostsModel(user: UserModel): UserPostsViewModel {
        val model: UserPostsViewModel by inject(
            parameters = { parametersOf(user) },
        )
        return model
    }

    fun getUserCommentsModel(user: UserModel): UserCommentsViewModel {
        val model: UserCommentsViewModel by inject(
            parameters = { parametersOf(user) },
        )
        return model
    }

    fun getCreateCommentModel(postId: Int, parentId: Int?): CreateCommentViewModel {
        val model: CreateCommentViewModel by inject(
            parameters = { parametersOf(postId, parentId) }
        )
        return model
    }

    fun getCreatePostModel(communityId: Int): CreatePostViewModel {
        val model: CreatePostViewModel by inject(
            parameters = { parametersOf(communityId) }
        )
        return model
    }
}
