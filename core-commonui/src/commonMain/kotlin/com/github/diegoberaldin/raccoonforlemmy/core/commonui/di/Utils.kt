package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
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

expect fun getCreatePostViewModel(
    editedPostId: Int?,
): CreatePostMviModel
