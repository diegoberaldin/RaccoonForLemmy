package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentResponse
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface CommentRepository {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_COMMENT_DEPTH = 6
    }

    suspend fun getAll(
        postId: Long? = null,
        auth: String? = null,
        instance: String? = null,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.All,
        sort: SortType = SortType.New,
        maxDepth: Int = MAX_COMMENT_DEPTH,
    ): List<CommentModel>?

    suspend fun getBy(
        id: Long,
        auth: String?,
        instance: String? = null,
    ): CommentModel?

    suspend fun getChildren(
        parentId: Long,
        auth: String? = null,
        instance: String? = null,
        limit: Int = DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.All,
        sort: SortType = SortType.New,
        maxDepth: Int = MAX_COMMENT_DEPTH,
    ): List<CommentModel>?

    fun asUpVoted(
        comment: CommentModel,
        voted: Boolean,
    ): CommentModel

    fun asUpVoted(
        mention: PersonMentionModel,
        voted: Boolean,
    ): PersonMentionModel

    suspend fun upVote(
        comment: CommentModel,
        auth: String,
        voted: Boolean,
    ): Result<CommentResponse>

    fun asDownVoted(
        comment: CommentModel,
        downVoted: Boolean,
    ): CommentModel

    fun asDownVoted(
        mention: PersonMentionModel,
        downVoted: Boolean,
    ): PersonMentionModel

    suspend fun downVote(
        comment: CommentModel,
        auth: String,
        downVoted: Boolean,
    ): Result<CommentResponse>

    fun asSaved(
        comment: CommentModel,
        saved: Boolean,
    ): CommentModel

    suspend fun save(
        comment: CommentModel,
        auth: String,
        saved: Boolean,
    ): Result<CommentResponse>

    suspend fun create(
        postId: Long,
        parentId: Long?,
        text: String,
        languageId: Long? = null,
        auth: String,
    )

    suspend fun edit(
        commentId: Long,
        text: String,
        languageId: Long? = null,
        auth: String,
    )

    suspend fun delete(
        commentId: Long,
        auth: String,
    ): CommentModel?

    suspend fun restore(
        commentId: Long,
        auth: String,
    ): CommentModel?

    suspend fun report(
        commentId: Long,
        reason: String,
        auth: String,
    )

    suspend fun remove(
        commentId: Long,
        auth: String,
        removed: Boolean,
        reason: String,
    ): CommentModel?

    suspend fun distinguish(
        commentId: Long,
        auth: String,
        distinguished: Boolean,
    ): CommentModel?

    suspend fun getReports(
        auth: String,
        communityId: Long? = null,
        page: Int,
        limit: Int = PostRepository.DEFAULT_PAGE_SIZE,
        unresolvedOnly: Boolean = true,
    ): List<CommentReportModel>?

    suspend fun resolveReport(
        reportId: Long,
        auth: String,
        resolved: Boolean,
    ): CommentReportModel?

    suspend fun purge(
        auth: String?,
        commentId: Long,
        reason: String? = null,
    )
}
