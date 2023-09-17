package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.comments.UserCommentsViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.posts.UserPostsViewModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject

actual fun getNavigationCoordinator(): NavigationCoordinator {
    val res: NavigationCoordinator by inject(NavigationCoordinator::class.java)
    return res
}


actual fun getPostDetailViewModel(post: PostModel): PostDetailViewModel {
    val res: PostDetailViewModel by inject(
        clazz = PostDetailViewModel::class.java,
        parameters = { parametersOf(post) },
    )
    return res
}

actual fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String,
): CommunityDetailViewModel {
    val res: CommunityDetailViewModel by inject(
        clazz = CommunityDetailViewModel::class.java,
        parameters = { parametersOf(community, otherInstance) },
    )
    return res
}

actual fun getCommunityInfoViewModel(community: CommunityModel): CommunityInfoViewModel {
    val res: CommunityInfoViewModel by inject(
        clazz = CommunityInfoViewModel::class.java,
        parameters = { parametersOf(community) },
    )
    return res
}

actual fun getInstanceInfoViewModel(url: String): InstanceInfoViewModel {
    val res: InstanceInfoViewModel by inject(
        clazz = InstanceInfoViewModel::class.java,
        parameters = { parametersOf(url) },
    )
    return res
}

actual fun getUserDetailViewModel(user: UserModel): UserDetailViewModel {
    val res: UserDetailViewModel by inject(
        clazz = UserDetailViewModel::class.java,
        parameters = { parametersOf(user) },
    )
    return res
}

actual fun getUserPostsViewModel(
    user: UserModel,
): UserPostsViewModel {
    val res: UserPostsViewModel by inject(
        clazz = UserPostsViewModel::class.java,
        parameters = { parametersOf(user) },
    )
    return res
}

actual fun getUserCommentsViewModel(user: UserModel): UserCommentsViewModel {
    val res: UserCommentsViewModel by inject(
        clazz = UserCommentsViewModel::class.java,
        parameters = { parametersOf(user) },
    )
    return res
}

actual fun getCreateCommentViewModel(postId: Int, parentId: Int?): CreateCommentViewModel {
    val res: CreateCommentViewModel by inject(
        clazz = CreateCommentViewModel::class.java,
        parameters = { parametersOf(postId, parentId) }
    )
    return res
}

actual fun getCreatePostViewModel(communityId: Int): CreatePostViewModel {
    val res: CreatePostViewModel by inject(
        clazz = CreatePostViewModel::class.java,
        parameters = { parametersOf(communityId) }
    )
    return res
}

actual fun getZoomableImageViewModel(): ZoomableImageViewModel {
    val res: ZoomableImageViewModel by inject(ZoomableImageViewModel::class.java)
    return res
}