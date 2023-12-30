package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.utils.cache.LruCache
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

internal class DefaultLemmyItemCache(
    private val postCache: LruCache<PostModel>,
    private val communityCache: LruCache<CommunityModel>,
    private val commentCache: LruCache<CommentModel>,
    private val userCache: LruCache<UserModel>,
) : LemmyItemCache {

    override suspend fun putPost(value: PostModel) {
        postCache.put(value = value, key = value.id)
    }

    override suspend fun getPost(id: Int): PostModel? = postCache.get(id)

    override suspend fun putComment(value: CommentModel) {
        commentCache.put(value = value, key = value.id)
    }

    override suspend fun getComment(id: Int): CommentModel? = commentCache.get(id)

    override suspend fun putCommunity(value: CommunityModel) {
        communityCache.put(value = value, key = value.id)
    }

    override suspend fun getCommunity(id: Int): CommunityModel? = communityCache.get(id)

    override suspend fun putUser(value: UserModel) {
        userCache.put(value = value, key = value.id)
    }

    override suspend fun getUser(id: Int): UserModel? = userCache.get(id)
}
