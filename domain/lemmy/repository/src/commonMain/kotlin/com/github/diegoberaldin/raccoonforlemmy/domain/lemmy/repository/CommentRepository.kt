package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentResponse
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import de.jensklingenberg.ktorfit.Response

interface CommentRepository {

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_COMMENT_DEPTH = 6
    }

    suspend fun getAll(
        postId: Int? = null,
        auth: String? = null,
        instance: String? = null,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.All,
        sort: SortType = SortType.New,
        maxDepth: Int = MAX_COMMENT_DEPTH,
    ): List<CommentModel>?

    suspend fun getBy(id: Int, auth: String?, instance: String? = null): CommentModel?

    suspend fun getChildren(
        parentId: Int,
        auth: String? = null,
        instance: String? = null,
        limit: Int = DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.All,
        sort: SortType = SortType.New,
        maxDepth: Int = MAX_COMMENT_DEPTH,
    ): List<CommentModel>?

    fun asUpVoted(comment: CommentModel, voted: Boolean): CommentModel
    fun asUpVoted(mention: PersonMentionModel, voted: Boolean): PersonMentionModel

    suspend fun upVote(comment: CommentModel, auth: String, voted: Boolean)
    fun asDownVoted(comment: CommentModel, downVoted: Boolean): CommentModel
    fun asDownVoted(mention: PersonMentionModel, downVoted: Boolean): PersonMentionModel

    suspend fun downVote(
        comment: CommentModel,
        auth: String,
        downVoted: Boolean,
    ): Result<Response<CommentResponse>>

    fun asSaved(comment: CommentModel, saved: Boolean): CommentModel

    suspend fun save(
        comment: CommentModel,
        auth: String,
        saved: Boolean,
    ): Result<Response<CommentResponse>>

    suspend fun create(
        postId: Int,
        parentId: Int?,
        text: String,
        languageId: Int? = null,
        auth: String,
    )

    suspend fun edit(
        commentId: Int,
        text: String,
        languageId: Int? = null,
        auth: String,
    )

    suspend fun delete(
        commentId: Int,
        auth: String,
    )

    suspend fun report(commentId: Int, reason: String, auth: String)

    suspend fun remove(
        commentId: Int,
        auth: String,
        removed: Boolean,
        reason: String,
    ): CommentModel?

    suspend fun distinguish(
        commentId: Int,
        auth: String,
        distinguished: Boolean,
    ): CommentModel?

    suspend fun getReports(
        auth: String,
        communityId: Int? = null,
        page: Int,
        limit: Int = PostRepository.DEFAULT_PAGE_SIZE,
        unresolvedOnly: Boolean = true,
    ): List<CommentReportModel>?

    suspend fun resolveReport(
        reportId: Int,
        auth: String,
        resolved: Boolean,
    ): CommentReportModel?
}
