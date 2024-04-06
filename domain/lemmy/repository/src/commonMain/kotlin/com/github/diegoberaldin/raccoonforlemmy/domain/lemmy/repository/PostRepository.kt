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
        communityId: Long? = null,
        communityName: String? = null,
        otherInstance: String? = null,
    ): Pair<List<PostModel>, String?>?

    suspend fun get(
        id: Long,
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
        communityId: Long,
        title: String,
        body: String?,
        url: String? = null,
        nsfw: Boolean = false,
        languageId: Long? = null,
        auth: String,
    )

    suspend fun edit(
        postId: Long,
        title: String,
        body: String?,
        url: String? = null,
        nsfw: Boolean = false,
        languageId: Long? = null,
        auth: String,
    )

    suspend fun setRead(
        read: Boolean,
        postId: Long,
        auth: String? = null
    ): Result<Response<PostResponse>>

    suspend fun delete(id: Long, auth: String)

    suspend fun uploadImage(auth: String, bytes: ByteArray): String?

    suspend fun report(postId: Long, reason: String, auth: String)

    suspend fun featureInCommunity(
        postId: Long,
        auth: String,
        featured: Boolean,
    ): PostModel?

    suspend fun lock(
        postId: Long,
        auth: String,
        locked: Boolean,
    ): PostModel?

    suspend fun remove(
        postId: Long,
        auth: String,
        reason: String,
        removed: Boolean,
    ): PostModel?

    suspend fun getReports(
        auth: String,
        communityId: Long? = null,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        unresolvedOnly: Boolean = true,
    ): List<PostReportModel>?

    suspend fun resolveReport(
        reportId: Long,
        auth: String,
        resolved: Boolean,
    ): PostReportModel?
}
