package com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.impl

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.DetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.UserDetailScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultDetailOpener(
    private val navigationCoordinator: NavigationCoordinator,
    private val itemCache: LemmyItemCache,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
) : DetailOpener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun openCommunityDetail(community: CommunityModel, otherInstance: String) {
        scope.launch {
            val (actualCommunity, actualInstance) = withContext(Dispatchers.IO) {
                val found = searchCommunity(community.name)
                if (found != null) {
                    found to ""
                } else {
                    community to otherInstance
                }
            }
            itemCache.putCommunity(actualCommunity)
            navigationCoordinator.pushScreen(
                CommunityDetailScreen(
                    communityId = actualCommunity.id,
                    otherInstance = actualInstance,
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

    private suspend fun searchCommunity(name: String): CommunityModel? {
        val auth = identityRepository.authToken.value

        tailrec suspend fun searchRec(page: Int = 0): CommunityModel? {
            val results = communityRepository.getAll(
                auth = auth,
                query = name,
                resultType = SearchResultType.Communities,
                page = page,
                limit = 50,
            )?.filterIsInstance<SearchResult.Community>().orEmpty()

            val found = results.firstOrNull {
                it.model.name == name
            }?.model
            // iterates for no more than 20 pages before giving up
            if (found != null || page >= 20) {
                return found
            }

            return searchRec(page + 1)
        }

        // start recursive search
        return searchRec()
    }
}