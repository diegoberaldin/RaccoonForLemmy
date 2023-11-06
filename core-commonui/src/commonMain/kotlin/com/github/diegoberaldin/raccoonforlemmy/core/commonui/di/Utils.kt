package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.TextToolbar
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat.InboxChatMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.DrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.ModalDrawerMviModel
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
import org.koin.core.module.Module

expect val imagePreloadModule: Module

expect fun getNavigationCoordinator(): NavigationCoordinator

expect fun getDrawerCoordinator(): DrawerCoordinator

expect fun getFabNestedScrollConnection(): FabNestedScrollConnection

expect fun getPostDetailViewModel(
    post: PostModel,
    otherInstance: String = "",
    highlightCommentId: Int? = null,
): PostDetailMviModel

expect fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String = "",
): CommunityDetailMviModel

expect fun getCommunityInfoViewModel(
    community: CommunityModel,
): CommunityInfoMviModel

expect fun getUserDetailViewModel(
    user: UserModel,
    otherInstance: String = "",
): UserDetailMviModel

expect fun getInstanceInfoViewModel(
    url: String,
): InstanceInfoMviModel

expect fun getCreateCommentViewModel(
    postId: Int? = null,
    parentId: Int? = null,
    editedCommentId: Int? = null,
): CreateCommentMviModel

expect fun getCreatePostViewModel(
    communityId: Int?,
    editedPostId: Int?,
): CreatePostMviModel

expect fun getZoomableImageViewModel(): ZoomableImageMviModel

expect fun getInboxChatViewModel(otherUserId: Int): InboxChatMviModel

expect fun getSavedItemsViewModel(): SavedItemsMviModel

expect fun getModalDrawerViewModel(): ModalDrawerMviModel

expect fun getCreateReportViewModel(
    postId: Int? = null,
    commentId: Int? = null,
): CreateReportMviModel

@Composable
expect fun getCustomTextToolbar(
    onSearch: () -> Unit,
): TextToolbar