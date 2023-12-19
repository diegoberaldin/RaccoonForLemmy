package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostResponse
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import de.jensklingenberg.ktorfit.Response

interface PostRepository {

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }

    suspend fun getAll(
        auth: String? = null,
        page: Int,
        pageCursor: String? = null,
        limit: Int = DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.Local,
        sort: SortType = SortType.Active,
        communityId: Int? = null,
        communityName: String? = null,
        otherInstance: String? = null,
    ): Pair<List<PostModel>, String?>?

    suspend fun get(
        id: Int,
        auth: String? = null,
        instance: String? = null,
    ): PostModel?

    fun asUpVoted(post: PostModel, voted: Boolean): PostModel

    suspend fun upVote(
        post: PostModel,
        auth: String,
        voted: Boolean
    ): Result<Response<PostResponse>>

    fun asDownVoted(post: PostModel, downVoted: Boolean): PostModel

    suspend fun downVote(
        post: PostModel,
        auth: String,
        downVoted: Boolean
    ): Result<Response<PostResponse>>

    fun asSaved(post: PostModel, saved: Boolean): PostModel

    suspend fun save(post: PostModel, auth: String, saved: Boolean): Result<Response<PostResponse>>

    suspend fun create(
        communityId: Int,
        title: String,
        body: String?,
        url: String? = null,
        nsfw: Boolean = false,
        auth: String,
    )

    suspend fun edit(
        postId: Int,
        title: String,
        body: String?,
        url: String? = null,
        nsfw: Boolean = false,
        auth: String,
    )

    suspend fun setRead(
        read: Boolean,
        postId: Int,
        auth: String? = null
    ): Result<Response<PostResponse>>

    suspend fun delete(id: Int, auth: String)

    suspend fun uploadImage(auth: String, bytes: ByteArray): String?

    suspend fun report(postId: Int, reason: String, auth: String)

    suspend fun featureInCommunity(
        postId: Int,
        auth: String,
        featured: Boolean,
    ): PostModel?

    suspend fun lock(
        postId: Int,
        auth: String,
        locked: Boolean,
    ): PostModel?

    suspend fun remove(
        postId: Int,
        auth: String,
        reason: String,
        removed: Boolean,
    ): PostModel?

    suspend fun getReports(
        auth: String,
        communityId: Int,
        page: Int,
        limit: Int = PostRepository.DEFAULT_PAGE_SIZE,
        unresolvedOnly: Boolean = true,
    ): List<PostReportModel>?

    suspend fun resolveReport(
        reportId: Int,
        auth: String,
        resolved: Boolean,
    ): PostReportModel?
}
