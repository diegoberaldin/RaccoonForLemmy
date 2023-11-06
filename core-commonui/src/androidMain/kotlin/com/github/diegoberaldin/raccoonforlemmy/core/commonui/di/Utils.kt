package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.CustomTextToolbar
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.DefaultImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat.InboxChatMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.DrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.ModalDrawerMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.report.CreateReportMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.saveditems.SavedItemsMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject

actual val imagePreloadModule = module {
    single<ImagePreloadManager> {
        DefaultImagePreloadManager(
            context = get(),
        )
    }
}

actual fun getNavigationCoordinator(): NavigationCoordinator {
    val res: NavigationCoordinator by inject(NavigationCoordinator::class.java)
    return res
}

actual fun getDrawerCoordinator(): DrawerCoordinator {
    val res: DrawerCoordinator by inject(DrawerCoordinator::class.java)
    return res
}

actual fun getFabNestedScrollConnection(): FabNestedScrollConnection {
    val res: FabNestedScrollConnection by inject(FabNestedScrollConnection::class.java)
    return res
}

actual fun getPostDetailViewModel(
    post: PostModel,
    otherInstance: String,
    highlightCommentId: Int?,
): PostDetailMviModel {
    val res: PostDetailMviModel by inject(
        clazz = PostDetailMviModel::class.java,
        parameters = { parametersOf(post, otherInstance, highlightCommentId) },
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

actual fun getCommunityInfoViewModel(community: CommunityModel): CommunityInfoMviModel {
    val res: CommunityInfoMviModel by inject(
        clazz = CommunityInfoMviModel::class.java,
        parameters = { parametersOf(community) },
    )
    return res
}

actual fun getInstanceInfoViewModel(url: String): InstanceInfoMviModel {
    val res: InstanceInfoMviModel by inject(
        clazz = InstanceInfoMviModel::class.java,
        parameters = { parametersOf(url) },
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

actual fun getCreatePostViewModel(communityId: Int?, editedPostId: Int?): CreatePostMviModel {
    val res: CreatePostMviModel by inject(clazz = CreatePostMviModel::class.java,
        parameters = { parametersOf(communityId, editedPostId) })
    return res
}

actual fun getZoomableImageViewModel(): ZoomableImageMviModel {
    val res: ZoomableImageMviModel by inject(ZoomableImageMviModel::class.java)
    return res
}

actual fun getInboxChatViewModel(otherUserId: Int): InboxChatMviModel {
    val res: InboxChatMviModel by inject(
        InboxChatMviModel::class.java,
        parameters = {
            parametersOf(otherUserId)
        },
    )
    return res
}

actual fun getSavedItemsViewModel(): SavedItemsMviModel {
    val res: SavedItemsMviModel by inject(
        clazz = SavedItemsMviModel::class.java,
    )
    return res
}

actual fun getModalDrawerViewModel(): ModalDrawerMviModel {
    val res: ModalDrawerMviModel by inject(ModalDrawerMviModel::class.java)
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


@Composable
actual fun getCustomTextToolbar(
    onSearch: () -> Unit,
): TextToolbar {
    return CustomTextToolbar(
        view = LocalView.current,
        onSearch = onSearch,
    )
}