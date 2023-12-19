package com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.impl

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.DetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.UserDetailScreen

class DefaultDetailOpener(
    private val navigationCoordinator: NavigationCoordinator,
) : DetailOpener {

    override fun openCommunityDetail(community: CommunityModel, otherInstance: String) {
        navigationCoordinator.pushScreen(
            CommunityDetailScreen(
                community = community,
                otherInstance = otherInstance,
            ),
        )
    }

    override fun openUserDetail(user: UserModel, otherInstance: String) {
        navigationCoordinator.pushScreen(
            UserDetailScreen(
                user = user,
                otherInstance = otherInstance,
            ),
        )
    }

    override fun openPostDetail(
        post: PostModel,
        otherInstance: String,
        highlightCommentId: Int?,
        isMod: Boolean,
    ) {
        navigationCoordinator.pushScreen(
            PostDetailScreen(
                post = post,
                highlightCommentId = highlightCommentId,
                otherInstance = otherInstance,
                isMod = isMod,
            ),
        )
    }
}