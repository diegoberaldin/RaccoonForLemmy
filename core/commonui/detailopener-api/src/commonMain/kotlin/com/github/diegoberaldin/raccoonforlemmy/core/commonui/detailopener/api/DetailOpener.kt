package com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api

import androidx.compose.runtime.Stable
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Stable
interface DetailOpener {
    fun openCommunityDetail(
        community: CommunityModel,
        otherInstance: String = "",
    )

    fun openUserDetail(
        user: UserModel,
        otherInstance: String = "",
    )

    fun openPostDetail(
        post: PostModel,
        otherInstance: String = "",
        highlightCommentId: Int? = null,
        isMod: Boolean = false,
    )

    fun openReply(
        draftId: Long? = null,
        originalPost: PostModel? = null,
        originalComment: CommentModel? = null,
        editedComment: CommentModel? = null,
        initialText: String? = null,
    )

    fun openCreatePost(
        draftId: Long? = null,
        editedPost: PostModel? = null,
        crossPost: PostModel? = null,
        communityId: Int? = null,
        initialText: String? = null,
        initialTitle: String? = null,
        initialUrl: String? = null,
        initialNsfw: Boolean? = null,
    )
}
