package com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.impl

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.DetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.UserDetailScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DefaultDetailOpener(
    private val navigationCoordinator: NavigationCoordinator,
    private val itemCache: LemmyItemCache,
) : DetailOpener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun openCommunityDetail(community: CommunityModel, otherInstance: String) {
        scope.launch {
            itemCache.putCommunity(community)
            navigationCoordinator.pushScreen(
                CommunityDetailScreen(
                    communityId = community.id,
                    otherInstance = otherInstance,
                ),
            )
        }
    }

    override fun openUserDetail(user: UserModel, otherInstance: String) {
        scope.launch {
            itemCache.putUser(user)
            navigationCoordinator.pushScreen(
                UserDetailScreen(
                    userId = user.id,
                    otherInstance = otherInstance,
                ),
            )
        }
    }

    override fun openPostDetail(
        post: PostModel,
        otherInstance: String,
        highlightCommentId: Int?,
        isMod: Boolean,
    ) {
        scope.launch {
            itemCache.putPost(post)
            navigationCoordinator.pushScreen(
                PostDetailScreen(
                    postId = post.id,
                    highlightCommentId = highlightCommentId,
                    otherInstance = otherInstance,
                    isMod = isMod,
                ),
            )
        }
    }

    override fun openReply(
        originalPost: PostModel?,
        originalComment: CommentModel?,
        editedComment: CommentModel?,
        initialText: String?,
    ) {
        scope.launch {
            if (originalPost != null) {
                itemCache.putPost(originalPost)
            }
            if (originalComment != null) {
                itemCache.putComment(originalComment)
            }
            if (editedComment != null) {
                itemCache.putComment(editedComment)
            }
            val screen = CreateCommentScreen(
                originalPostId = originalPost?.id,
                originalCommentId = originalComment?.id,
                editedCommentId = editedComment?.id,
                initialText = initialText,
            )
            navigationCoordinator.pushScreen(screen)
        }
    }

    override fun openCreatePost(
        editedPost: PostModel?,
        crossPost: PostModel?,
        communityId: Int?,
    ) {
        scope.launch {
            if (editedPost != null) {
                itemCache.putPost(editedPost)
            }
            if (crossPost != null) {
                itemCache.putPost(crossPost)
            }
            val screen = CreatePostScreen(
                editedPostId = editedPost?.id,
                crossPostId = crossPost?.id,
                communityId = communityId,
            )
            navigationCoordinator.pushScreen(screen)
        }
    }
}