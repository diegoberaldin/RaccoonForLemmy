package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentLikeForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeleteCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SaveCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toCommentDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

class CommentRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_COMMENT_DEPTH = 6
    }

    suspend fun getAll(
        postId: Int,
        auth: String? = null,
        instance: String? = null,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.All,
        sort: SortType = SortType.New,
        maxDepth: Int = MAX_COMMENT_DEPTH,
    ): List<CommentModel>? = runCatching {
        val response = if (instance.isNullOrEmpty()) {
            services.comment.getAll(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                postId = postId,
                page = page,
                limit = limit,
                type = type.toDto(),
                sort = sort.toCommentDto(),
                maxDepth = maxDepth,
            )
        } else {
            customServices.changeInstance(instance)
            customServices.comment.getAll(
                postId = postId,
                page = page,
                limit = limit,
                type = type.toDto(),
                sort = sort.toCommentDto(),
                maxDepth = maxDepth,
            )
        }
        val dto = response.body()?.comments ?: emptyList()
        dto.map { it.toModel() }
    }.getOrNull()

    suspend fun getBy(id: Int, auth: String?, instance: String? = null): CommentModel? =
        runCatching {
            if (instance.isNullOrEmpty()) {
                services.comment.getBy(
                    authHeader = auth.toAuthHeader(),
                    id = id,
                    auth = auth,
                ).body()
            } else {
                customServices.changeInstance(instance)
                customServices.comment.getBy(id = id).body()
            }?.commentView?.toModel()
        }.getOrNull()

    suspend fun getChildren(
        parentId: Int,
        auth: String? = null,
        instance: String? = null,
        limit: Int = PostRepository.DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.All,
        sort: SortType = SortType.New,
        maxDepth: Int = 1,
    ): List<CommentModel>? = runCatching {
        val response = if (instance.isNullOrEmpty()) {
            services.comment.getAll(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                parentId = parentId,
                limit = limit,
                type = type.toDto(),
                sort = sort.toCommentDto(),
                maxDepth = maxDepth,
            )
        } else {
            customServices.changeInstance(instance)
            customServices.comment.getAll(
                parentId = parentId,
                limit = limit,
                type = type.toDto(),
                sort = sort.toCommentDto(),
                maxDepth = maxDepth,
            )
        }
        val dto = response.body()?.comments ?: emptyList()
        dto.map { it.toModel() }
    }.getOrNull()

    fun asUpVoted(comment: CommentModel, voted: Boolean) = comment.copy(
        myVote = if (voted) 1 else 0,
        score = when {
            voted && comment.myVote < 0 -> comment.score + 2
            voted -> comment.score + 1
            !voted -> comment.score - 1
            else -> comment.score
        },
        upvotes = when {
            voted -> comment.upvotes + 1
            else -> comment.upvotes - 1
        },
        downvotes = when {
            comment.myVote < 0 -> comment.downvotes - 1
            else -> comment.downvotes
        }
    )

    fun asUpVoted(mention: PersonMentionModel, voted: Boolean) = mention.copy(
        myVote = if (voted) 1 else 0,
        score = when {
            voted && mention.myVote < 0 -> mention.score + 2
            voted -> mention.score + 1
            !voted -> mention.score - 1
            else -> mention.score
        },
        upvotes = when {
            voted -> mention.upvotes + 1
            else -> mention.upvotes - 1
        },
        downvotes = when {
            mention.myVote < 0 -> mention.downvotes - 1
            else -> mention.downvotes
        }
    )

    suspend fun upVote(comment: CommentModel, auth: String, voted: Boolean) {
        val data = CreateCommentLikeForm(
            commentId = comment.id,
            score = if (voted) 1 else 0,
            auth = auth,
        )
        services.comment.like(authHeader = auth.toAuthHeader(), form = data)
    }

    fun asDownVoted(comment: CommentModel, downVoted: Boolean) = comment.copy(
        myVote = if (downVoted) -1 else 0,
        score = when {
            downVoted && comment.myVote > 0 -> comment.score - 2
            downVoted -> comment.score - 1
            !downVoted -> comment.score + 1
            else -> comment.score
        },
        downvotes = when {
            downVoted -> comment.downvotes + 1
            else -> comment.downvotes - 1
        },
        upvotes = when {
            comment.myVote > 0 -> comment.upvotes - 1
            else -> comment.upvotes
        }
    )

    fun asDownVoted(mention: PersonMentionModel, downVoted: Boolean) = mention.copy(
        myVote = if (downVoted) -1 else 0,
        score = when {
            downVoted && mention.myVote > 0 -> mention.score - 2
            downVoted -> mention.score - 1
            !downVoted -> mention.score + 1
            else -> mention.score
        },
        downvotes = when {
            downVoted -> mention.downvotes + 1
            else -> mention.downvotes - 1
        },
        upvotes = when {
            mention.myVote > 0 -> mention.upvotes - 1
            else -> mention.upvotes
        }
    )

    suspend fun downVote(comment: CommentModel, auth: String, downVoted: Boolean) = runCatching {
        val data = CreateCommentLikeForm(
            commentId = comment.id,
            score = if (downVoted) -1 else 0,
            auth = auth,
        )
        services.comment.like(authHeader = auth.toAuthHeader(), form = data)
    }

    fun asSaved(comment: CommentModel, saved: Boolean) = comment.copy(saved = saved)

    suspend fun save(comment: CommentModel, auth: String, saved: Boolean) = runCatching {
        val data = SaveCommentForm(
            commentId = comment.id,
            save = saved,
            auth = auth,
        )
        services.comment.save(authHeader = auth.toAuthHeader(), form = data)
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
        services.comment.create(authHeader = auth.toAuthHeader(), form = data)
    }

    suspend fun edit(
        commentId: Int,
        text: String,
        auth: String,
    ) {
        val data = EditCommentForm(
            content = text,
            commentId = commentId,
            auth = auth,
        )
        services.comment.edit(authHeader = auth.toAuthHeader(), form = data)
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
        services.comment.delete(authHeader = auth.toAuthHeader(), form = data)
    }
}
