package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostLikeForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostReportForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeletePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditPostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.FeaturePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.LockPostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPostAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostFeatureType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.RemovePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ResolvePostReportForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SavePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultPostRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) : PostRepository {

    override suspend fun getAll(
        auth: String?,
        page: Int,
        pageCursor: String?,
        limit: Int,
        type: ListingType,
        sort: SortType,
        communityId: Long?,
        communityName: String?,
        otherInstance: String?,
    ): Pair<List<PostModel>, String?>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = if (otherInstance.isNullOrEmpty()) {
                services.post.getAll(
                    authHeader = auth.toAuthHeader(),
                    auth = auth,
                    communityId = communityId,
                    page = if (pageCursor.isNullOrEmpty()) page else null,
                    pageCursor = pageCursor,
                    limit = limit,
                    type = type.toDto(),
                    sort = sort.toDto(),
                )
            } else {
                customServices.changeInstance(otherInstance)
                customServices.post.getAll(
                    communityName = communityName,
                    page = if (pageCursor.isNullOrEmpty()) page else null,
                    pageCursor = pageCursor,
                    limit = limit,
                    type = type.toDto(),
                    sort = sort.toDto(),
                )
            }
            val body = response.body()
            val posts = body?.posts?.map { it.toModel() } ?: emptyList()
            posts to body?.nextPage
        }.getOrNull()
    }

    override suspend fun get(
        id: Long,
        auth: String?,
        instance: String?,
    ): PostModel? = withContext(Dispatchers.IO) {
        runCatching {
            val response = if (instance.isNullOrEmpty()) {
                services.post.get(
                    authHeader = auth.toAuthHeader(),
                    auth = auth,
                    id = id,
                ).body()
            } else {
                customServices.changeInstance(instance)
                customServices.post.get(id = id).body()
            }
            val dto = response?.postView
            dto?.toModel()?.copy(
                crossPosts = response.crossPosts.map { it.toModel() }
            )
        }.getOrNull()
    }

    override fun asUpVoted(post: PostModel, voted: Boolean) = post.copy(
        myVote = if (voted) 1 else 0,
        score = when {
            voted && post.myVote < 0 -> post.score + 2
            voted -> post.score + 1
            !voted -> post.score - 1
            else -> post.score
        },
        upvotes = when {
            voted -> post.upvotes + 1
            else -> post.upvotes - 1
        },
        downvotes = when {
            post.myVote < 0 -> post.downvotes - 1
            else -> post.downvotes
        }
    )

    override suspend fun upVote(post: PostModel, auth: String, voted: Boolean) =
        withContext(Dispatchers.IO) {
            runCatching {
                val data = CreatePostLikeForm(
                    postId = post.id,
                    score = if (voted) 1 else 0,
                    auth = auth,
                )
                services.post.like(
                    authHeader = auth.toAuthHeader(),
                    form = data,
                )
            }
        }

    override fun asDownVoted(post: PostModel, downVoted: Boolean) = post.copy(
        myVote = if (downVoted) -1 else 0,
        score = when {
            downVoted && post.myVote > 0 -> post.score - 2
            downVoted -> post.score - 1
            !downVoted -> post.score + 1
            else -> post.score
        },
        downvotes = when {
            downVoted -> post.downvotes + 1
            else -> post.downvotes - 1
        },
        upvotes = when {
            post.myVote > 0 -> post.upvotes - 1
            else -> post.upvotes
        }
    )

    override suspend fun downVote(post: PostModel, auth: String, downVoted: Boolean) =
        withContext(Dispatchers.IO) {
            runCatching {
                val data = CreatePostLikeForm(
                    postId = post.id,
                    score = if (downVoted) -1 else 0,
                    auth = auth,
                )
                services.post.like(
                    authHeader = auth.toAuthHeader(),
                    form = data,
                )
            }
        }

    override fun asSaved(post: PostModel, saved: Boolean): PostModel = post.copy(saved = saved)

    override suspend fun save(post: PostModel, auth: String, saved: Boolean) =
        withContext(Dispatchers.IO) {
            runCatching {
                val data = SavePostForm(
                    postId = post.id,
                    save = saved,
                    auth = auth,
                )
                services.post.save(
                    authHeader = auth.toAuthHeader(),
                    form = data,
                )
            }
        }

    override suspend fun create(
        communityId: Long,
        title: String,
        body: String?,
        url: String?,
        nsfw: Boolean,
        languageId: Long?,
        auth: String,
    ): Unit = withContext(Dispatchers.IO) {
        val data = CreatePostForm(
            communityId = communityId,
            name = title,
            body = body,
            url = url,
            nsfw = nsfw,
            languageId = languageId,
            auth = auth,
        )
        services.post.create(
            authHeader = auth.toAuthHeader(),
            form = data,
        )
    }

    override suspend fun edit(
        postId: Long,
        title: String,
        body: String?,
        url: String?,
        nsfw: Boolean,
        languageId: Long?,
        auth: String,
    ): Unit = withContext(Dispatchers.IO) {
        val data = EditPostForm(
            postId = postId,
            name = title,
            body = body,
            url = url,
            nsfw = nsfw,
            languageId = languageId,
            auth = auth,
        )
        services.post.edit(
            authHeader = auth.toAuthHeader(),
            form = data,
        )
    }

    override suspend fun setRead(read: Boolean, postId: Long, auth: String?) =
        withContext(Dispatchers.IO) {
            runCatching {
                val data = MarkPostAsReadForm(
                    postId = postId,
                    read = read,
                    auth = auth.orEmpty(),
                )
                services.post.markAsRead(
                    authHeader = auth.toAuthHeader(),
                    form = data,
                )
            }
        }

    override suspend fun delete(id: Long, auth: String): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val data = DeletePostForm(
                postId = id,
                deleted = true,
            )
            services.post.delete(
                authHeader = auth.toAuthHeader(),
                form = data,
            )
        }.apply {
            exceptionOrNull()?.also {
                it.printStackTrace()
            }
        }.getOrDefault(Unit)
    }

    override suspend fun uploadImage(auth: String, bytes: ByteArray): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://${services.currentInstance}/pictrs/image"
                val multipart = MultiPartFormDataContent(formData {
                    append(key = "images[]", value = bytes, headers = Headers.build {
                        append(HttpHeaders.ContentType, "image/*")
                        append(HttpHeaders.ContentDisposition, "filename=image.jpeg")
                    })
                })
                val images = services.post.uploadImage(
                    url = url,
                    token = "jwt=$auth",
                    authHeader = auth.toAuthHeader(),
                    content = multipart,
                ).body()
                "$url/${images?.files?.firstOrNull()?.file}"
            }.apply {
                exceptionOrNull()?.also {
                    it.printStackTrace()
                }
            }.getOrNull()
        }

    override suspend fun report(postId: Long, reason: String, auth: String): Unit =
        withContext(Dispatchers.IO) {
            runCatching {
                val data = CreatePostReportForm(
                    postId = postId,
                    reason = reason,
                    auth = auth,
                )
                services.post.createReport(
                    form = data,
                    authHeader = auth.toAuthHeader(),
                )
            }
        }

    override suspend fun featureInCommunity(
        postId: Long,
        auth: String,
        featured: Boolean,
    ): PostModel? = withContext(Dispatchers.IO) {
        runCatching {
            val data = FeaturePostForm(
                postId = postId,
                auth = auth,
                featured = featured,
                featureType = PostFeatureType.Community,
            )
            val response = services.post.feature(
                form = data,
                authHeader = auth.toAuthHeader(),
            )
            response.body()?.postView?.toModel()
        }.getOrNull()
    }

    override suspend fun lock(
        postId: Long,
        auth: String,
        locked: Boolean,
    ): PostModel? = withContext(Dispatchers.IO) {
        runCatching {
            val data = LockPostForm(
                postId = postId,
                auth = auth,
                locked = locked,
            )
            val response = services.post.lock(
                form = data,
                authHeader = auth.toAuthHeader(),
            )
            response.body()?.postView?.toModel()
        }.getOrNull()
    }

    override suspend fun remove(
        postId: Long,
        auth: String,
        reason: String,
        removed: Boolean,
    ): PostModel? = runCatching {
        val data = RemovePostForm(
            postId = postId,
            auth = auth,
            removed = removed,
            reason = reason,
        )
        val response = services.post.remove(
            form = data,
            authHeader = auth.toAuthHeader(),
        )
        response.body()?.postView?.toModel()
    }.getOrNull()

    override suspend fun getReports(
        auth: String,
        communityId: Long?,
        page: Int,
        limit: Int,
        unresolvedOnly: Boolean,
    ): List<PostReportModel>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = services.post.listReports(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                communityId = communityId,
                page = page,
                limit = limit,
                unresolvedOnly = unresolvedOnly
            )
            response.body()?.postReports?.map {
                it.toModel()
            }
        }.getOrNull()
    }

    override suspend fun resolveReport(
        reportId: Long,
        auth: String,
        resolved: Boolean,
    ): PostReportModel? = withContext(Dispatchers.IO) {
        runCatching {
            val data = ResolvePostReportForm(
                reportId = reportId,
                auth = auth,
                resolved = resolved,
            )
            val response = services.post.resolveReport(
                form = data,
                authHeader = auth.toAuthHeader(),
            )
            response.body()?.postReportView?.toModel()
        }.getOrNull()
    }
}
