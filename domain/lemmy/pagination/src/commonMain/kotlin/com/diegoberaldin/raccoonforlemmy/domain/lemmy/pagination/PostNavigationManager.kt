package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel

interface PostNavigationManager {
    fun setPagination(state: PostPaginationManagerState)
    suspend fun getPrevious(postId: Long): PostModel?
    suspend fun getNext(postId: Long): PostModel?
}
