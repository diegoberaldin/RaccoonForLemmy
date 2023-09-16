package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentLikeForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeleteCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SaveCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toCommentDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

class CommentRepository(
    private val services: ServiceProvider,
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_COMMENT_DEPTH = 5
    }

    suspend fun getAll(
        postId: Int,
        auth: String? = null,
        page: Int,
        limit: Int = PostsRepository.DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.All,
        sort: SortType = SortType.New,
        maxDepth: Int = 1,
    ): List<CommentModel> = runCatching {
        val response = services.comment.getAll(
            auth = auth,
            postId = postId,
            page = page,
            limit = limit,
            type = type.toDto(),
            sort = sort.toCommentDto(),
            maxDepth = maxDepth,
        )
        val dto = response.body()?.comments ?: emptyList()
        dto.map { it.toModel() }
    }.getOrElse { emptyList() }

    suspend fun getChildren(
        parentId: Int,
        auth: String? = null,
        limit: Int = PostsRepository.DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.All,
        sort: SortType = SortType.New,
        maxDepth: Int = 1,
    ): List<CommentModel> = runCatching {
        val response = services.comment.getAll(
            auth = auth,
            parentId = parentId,
            limit = limit,
            type = type.toDto(),
            sort = sort.toCommentDto(),
            maxDepth = maxDepth,
        )
        val dto = response.body()?.comments ?: emptyList()
        dto.map { it.toModel() }
    }.getOrElse { emptyList() }

    fun asUpVoted(comment: CommentModel, voted: Boolean) = comment.copy(
        myVote = if (voted) 1 else 0,
        score = when {
            voted && comment.myVote < 0 -> comment.score + 2
            voted -> comment.score + 1
            !voted -> comment.score - 1
            else -> comment.score
        },
    )

    suspend fun upVote(comment: CommentModel, auth: String, voted: Boolean) {
        val data = CreateCommentLikeForm(
            commentId = comment.id,
            score = if (voted) 1 else 0,
            auth = auth,
        )
        services.comment.like(data)
    }

    fun asDownVoted(comment: CommentModel, downVoted: Boolean) = comment.copy(
        myVote = if (downVoted) -1 else 0,
        score = when {
            downVoted && comment.myVote > 0 -> comment.score - 2
            downVoted -> comment.score - 1
            !downVoted -> comment.score + 1
            else -> comment.score
        },
    )

    suspend fun downVote(comment: CommentModel, auth: String, downVoted: Boolean) = runCatching {
        val data = CreateCommentLikeForm(
            commentId = comment.id,
            score = if (downVoted) -1 else 0,
            auth = auth,
        )
        services.comment.like(data)
    }

    fun asSaved(comment: CommentModel, saved: Boolean) = comment.copy(saved = saved)

    suspend fun save(comment: CommentModel, auth: String, saved: Boolean) = runCatching {
        val data = SaveCommentForm(
            commentId = comment.id,
            save = saved,
            auth = auth,
        )
        services.comment.save(data)
    }

    suspend fun create(
        postId: Int,
        parentId: Int?,
        text: String,
        auth: String,
    ) {
        val data = CreateCommentForm(
            content = text,
            postId = postId,
            parentId = parentId,
            auth = auth,
        )
        services.comment.create(data)
    }

    suspend fun delete(
        commentId: Int,
        auth: String,
    ) {
        val data = DeleteCommentForm(
            commentId = commentId,
            deleted = true,
            auth = auth
        )
        services.comment.delete(data)
    }
}
