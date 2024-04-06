package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

interface LemmyItemCache {
    suspend fun putPost(value: PostModel)
    suspend fun getPost(id: Long): PostModel?

    suspend fun putComment(value: CommentModel)
    suspend fun getComment(id: Long): CommentModel?

    suspend fun putCommunity(value: CommunityModel)
    suspend fun getCommunity(id: Long): CommunityModel?

    suspend fun putUser(value: UserModel)
    suspend fun getUser(id: Long): UserModel?
}
