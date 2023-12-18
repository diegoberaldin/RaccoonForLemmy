package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createreport.CreateReportMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.remove.RemoveMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.reportlist.ReportListMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.saveditems.SavedItemsMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

expect fun getPostDetailViewModel(
    post: PostModel,
    otherInstance: String = "",
    highlightCommentId: Int? = null,
    isModerator: Boolean = false,
): PostDetailMviModel

expect fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String = "",
): CommunityDetailMviModel

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
    editedPostId: Int?,
): CreatePostMviModel

expect fun getSavedItemsViewModel(): SavedItemsMviModel

expect fun getCreateReportViewModel(
    postId: Int? = null,
    commentId: Int? = null,
): CreateReportMviModel

expect fun getRemoveViewModel(
    postId: Int? = null,
    commentId: Int? = null,
): RemoveMviModel

expect fun getReportListViewModel(
    communityId: Int,
): ReportListMviModel
