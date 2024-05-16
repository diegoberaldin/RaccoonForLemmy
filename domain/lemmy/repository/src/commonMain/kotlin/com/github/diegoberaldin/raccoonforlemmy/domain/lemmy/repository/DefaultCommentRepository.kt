package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentLikeForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentReportForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeleteCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DistinguishCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PurgeCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.RemoveCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ResolveCommentReportForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SaveCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toCommentDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultCommentRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) : CommentRepository {
    override suspend fun getAll(
        postId: Long?,
        auth: String?,
        instance: String?,
        page: Int,
        limit: Int,
        type: ListingType,
        sort: SortType,
        maxDepth: Int,
    ): List<CommentModel>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    if (instance.isNullOrEmpty()) {
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
        }

    override suspend fun getBy(
        id: Long,
        auth: String?,
        instance: String?,
    ): CommentModel? =
        withContext(Dispatchers.IO) {
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
        }

    override suspend fun getChildren(
        parentId: Long,
        auth: String?,
        instance: String?,
        limit: Int,
        type: ListingType,
        sort: SortType,
        maxDepth: Int,
    ): List<CommentModel>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    if (instance.isNullOrEmpty()) {
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
        }

    override fun asUpVoted(
        comment: CommentModel,
        voted: Boolean,
    ) = comment.copy(
        myVote = if (voted) 1 else 0,
        score =
            when {
                voted && comment.myVote < 0 -> comment.score + 2
                voted -> comment.score + 1
                !voted -> comment.score - 1
                else -> comment.score
            },
        upvotes =
            when {
                voted -> comment.upvotes + 1
                else -> comment.upvotes - 1
            },
        downvotes =
            when {
                comment.myVote < 0 -> comment.downvotes - 1
                else -> comment.downvotes
            },
    )

    override fun asUpVoted(
        mention: PersonMentionModel,
        voted: Boolean,
    ) = mention.copy(
        myVote = if (voted) 1 else 0,
        score =
            when {
                voted && mention.myVote < 0 -> mention.score + 2
                voted -> mention.score + 1
                !voted -> mention.score - 1
                else -> mention.score
            },
        upvotes =
            when {
                voted -> mention.upvotes + 1
                else -> mention.upvotes - 1
            },
        downvotes =
            when {
                mention.myVote < 0 -> mention.downvotes - 1
                else -> mention.downvotes
            },
    )

    override suspend fun upVote(
        comment: CommentModel,
        auth: String,
        voted: Boolean,
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val data =
                CreateCommentLikeForm(
                    commentId = comment.id,
                    score = if (voted) 1 else 0,
                    auth = auth,
                )
            services.comment.like(authHeader = auth.toAuthHeader(), form = data)
        }
    }

    override fun asDownVoted(
        comment: CommentModel,
        downVoted: Boolean,
    ) = comment.copy(
        myVote = if (downVoted) -1 else 0,
        score =
            when {
                downVoted && comment.myVote > 0 -> comment.score - 2
                downVoted -> comment.score - 1
                !downVoted -> comment.score + 1
                else -> comment.score
            },
        downvotes =
            when {
                downVoted -> comment.downvotes + 1
                else -> comment.downvotes - 1
            },
        upvotes =
            when {
                comment.myVote > 0 -> comment.upvotes - 1
                else -> comment.upvotes
            },
    )

    override fun asDownVoted(
        mention: PersonMentionModel,
        downVoted: Boolean,
    ) = mention.copy(
        myVote = if (downVoted) -1 else 0,
        score =
            when {
                downVoted && mention.myVote > 0 -> mention.score - 2
                downVoted -> mention.score - 1
                !downVoted -> mention.score + 1
                else -> mention.score
            },
        downvotes =
            when {
                downVoted -> mention.downvotes + 1
                else -> mention.downvotes - 1
            },
        upvotes =
            when {
                mention.myVote > 0 -> mention.upvotes - 1
                else -> mention.upvotes
            },
    )

    override suspend fun downVote(
        comment: CommentModel,
        auth: String,
        downVoted: Boolean,
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val data =
                CreateCommentLikeForm(
                    commentId = comment.id,
                    score = if (downVoted) -1 else 0,
                    auth = auth,
                )
            services.comment.like(authHeader = auth.toAuthHeader(), form = data)
        }
    }

    override fun asSaved(
        comment: CommentModel,
        saved: Boolean,
    ) = comment.copy(saved = saved)

    override suspend fun save(
        comment: CommentModel,
        auth: String,
        saved: Boolean,
    ) = runCatching {
        val data =
            SaveCommentForm(
                commentId = comment.id,
                save = saved,
                auth = auth,
            )
        services.comment.save(authHeader = auth.toAuthHeader(), form = data)
    }

    override suspend fun create(
        postId: Long,
        parentId: Long?,
        text: String,
        languageId: Long?,
        auth: String,
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val data =
                CreateCommentForm(
                    content = text,
                    postId = postId,
                    parentId = parentId,
                    languageId = languageId,
                    auth = auth,
                )
            services.comment.create(authHeader = auth.toAuthHeader(), form = data)
            Unit
        }.getOrDefault(Unit)
    }

    override suspend fun edit(
        commentId: Long,
        text: String,
        languageId: Long?,
        auth: String,
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val data =
                EditCommentForm(
                    content = text,
                    commentId = commentId,
                    languageId = languageId,
                    auth = auth,
                )
            services.comment.edit(authHeader = auth.toAuthHeader(), form = data)
            Unit
        }.getOrDefault(Unit)
    }

    override suspend fun delete(
        commentId: Long,
        auth: String,
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val data =
                DeleteCommentForm(
                    commentId = commentId,
                    deleted = true,
                )
            services.comment.delete(authHeader = auth.toAuthHeader(), form = data)
            Unit
        }.getOrDefault(Unit)
    }

    override suspend fun report(
        commentId: Long,
        reason: String,
        auth: String,
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val data =
                CreateCommentReportForm(
                    commentId = commentId,
                    reason = reason,
                    auth = auth,
                )
            services.comment.createReport(
                form = data,
                authHeader = auth.toAuthHeader(),
            )
            Unit
        }.getOrDefault(Unit)
    }

    override suspend fun remove(
        commentId: Long,
        auth: String,
        removed: Boolean,
        reason: String,
    ): CommentModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    RemoveCommentForm(
                        commentId = commentId,
                        removed = removed,
                        reason = reason,
                        auth = auth,
                    )
                val response =
                    services.comment.remove(
                        form = data,
                        authHeader = auth.toAuthHeader(),
                    )
                response.body()?.commentView?.toModel()
            }.getOrNull()
        }

    override suspend fun distinguish(
        commentId: Long,
        auth: String,
        distinguished: Boolean,
    ): CommentModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    DistinguishCommentForm(
                        commentId = commentId,
                        distinguished = distinguished,
                        auth = auth,
                    )
                val response =
                    services.comment.distinguish(
                        form = data,
                        authHeader = auth.toAuthHeader(),
                    )
                response.body()?.commentView?.toModel()
            }.getOrNull()
        }

    override suspend fun getReports(
        auth: String,
        communityId: Long?,
        page: Int,
        limit: Int,
        unresolvedOnly: Boolean,
    ): List<CommentReportModel>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.comment.listReports(
                        authHeader = auth.toAuthHeader(),
                        auth = auth,
                        communityId = communityId,
                        page = page,
                        limit = limit,
                        unresolvedOnly = unresolvedOnly,
                    )
                response.body()?.commentReports?.map {
                    it.toModel()
                }
            }.getOrNull()
        }

    override suspend fun resolveReport(
        reportId: Long,
        auth: String,
        resolved: Boolean,
    ): CommentReportModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    ResolveCommentReportForm(
                        reportId = reportId,
                        resolved = resolved,
                        auth = auth,
                    )
                val response =
                    services.comment.resolveReport(
                        form = data,
                        authHeader = auth.toAuthHeader(),
                    )
                response.body()?.commentReportView?.toModel()
            }.getOrNull()
        }

    override suspend fun purge(
        auth: String?,
        commentId: Long,
        reason: String?,
    ) = withContext(Dispatchers.IO) {
        val data =
            PurgeCommentForm(
                commentId = commentId,
                reason = reason,
            )
        val response =
            services.comment.purge(
                form = data,
                authHeader = auth.toAuthHeader(),
            )
        require(response.body()?.success == true)
    }
}
