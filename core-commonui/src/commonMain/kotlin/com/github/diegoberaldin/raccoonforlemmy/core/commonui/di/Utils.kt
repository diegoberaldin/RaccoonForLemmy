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

expect fun getPostDetailViewModel(
    post: PostModel,
): PostDetailViewModel

expect fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String = "",
): CommunityDetailViewModel

expect fun getCommunityInfoViewModel(
    community: CommunityModel,
): CommunityInfoViewModel

expect fun getUserDetailViewModel(
    user: UserModel,
): UserDetailViewModel

expect fun getUserPostsViewModel(
    user: UserModel,
): UserPostsViewModel

expect fun getUserCommentsViewModel(
    user: UserModel,
): UserCommentsViewModel

expect fun getInstanceInfoViewModel(
    url: String,
): InstanceInfoViewModel

expect fun getCreateCommentViewModel(
    postId: Int,
    parentId: Int? = null,
): CreateCommentViewModel

expect fun getCreatePostViewModel(
    communityId: Int,
): CreatePostViewModel