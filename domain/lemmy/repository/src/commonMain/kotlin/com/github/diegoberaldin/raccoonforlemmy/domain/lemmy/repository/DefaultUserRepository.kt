package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockPersonForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkAllAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkCommentAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPersonMentionAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PurgePersonForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toCommentDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultUserRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) : UserRepository {

    override suspend fun getResolved(query: String, auth: String?): UserModel? = withContext(Dispatchers.IO) {
        runCatching {
            val response = services.search.resolveObject(
                authHeader = auth,
                q = query,
            ).body()
            response?.user?.toModel()
        }.getOrNull()
    }

    override suspend fun get(
        id: Long,
        auth: String?,
        username: String?,
        otherInstance: String?,
    ): UserModel? = withContext(Dispatchers.IO) {
        runCatching {
            val response = if (otherInstance.isNullOrEmpty()) {
                services.user.getDetails(
                    authHeader = auth.toAuthHeader(),
                    auth = auth,
                    personId = id,
                )
            } else {
                customServices.changeInstance(otherInstance)
                customServices.user.getDetails(
                    username = "$username@$otherInstance",
                )
            }
            val dto = response.body() ?: return@runCatching null
            dto.personView.toModel()
        }.getOrNull()
    }

    override suspend fun getPosts(
        id: Long?,
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
        username: String?,
        otherInstance: String?,
    ): List<PostModel>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = if (otherInstance.isNullOrEmpty()) {
                services.user.getDetails(
                    authHeader = auth.toAuthHeader(),
                    auth = auth,
                    personId = id,
                    page = page,
                    limit = limit,
                    sort = sort.toCommentDto(),
                )
            } else {
                customServices.changeInstance(otherInstance)
                customServices.user.getDetails(
                    username = "$username@$otherInstance",
                    page = page,
                    limit = limit,
                    sort = sort.toCommentDto(),
                )
            }
            val dto = response.body() ?: return@runCatching emptyList()
            dto.posts.map { it.toModel() }
        }.getOrNull()
    }

    override suspend fun getSavedPosts(
        id: Long,
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
    ): List<PostModel>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = services.user.getDetails(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                personId = id,
                page = page,
                limit = limit,
                sort = sort.toCommentDto(),
                savedOnly = true,
            )
            val dto = response.body() ?: return@runCatching emptyList()
            dto.posts.map { it.toModel() }
        }.getOrNull()
    }

    override suspend fun getComments(
        id: Long?,
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
        username: String?,
        otherInstance: String?,
    ): List<CommentModel>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = if (otherInstance.isNullOrEmpty()) {
                services.user.getDetails(
                    authHeader = auth.toAuthHeader(),
                    auth = auth,
                    personId = id,
                    page = page,
                    limit = limit,
                    sort = sort.toCommentDto(),
                )
            } else {
                customServices.changeInstance(otherInstance)
                customServices.user.getDetails(
                    username = "$username@$otherInstance",
                    page = page,
                    limit = limit,
                    sort = sort.toCommentDto(),
                )
            }
            val dto = response.body() ?: return@runCatching emptyList()
            dto.comments.map { it.toModel() }
        }.getOrNull()
    }

    override suspend fun getSavedComments(
        id: Long,
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
    ): List<CommentModel>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = services.user.getDetails(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                personId = id,
                page = page,
                limit = limit,
                sort = sort.toCommentDto(),
                savedOnly = true,
            )
            val dto = response.body() ?: return@runCatching emptyList()
            dto.comments.map { it.toModel() }
        }.getOrNull()
    }

    override suspend fun getMentions(
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
        unreadOnly: Boolean,
    ): List<PersonMentionModel>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = services.user.getMentions(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                limit = limit,
                sort = sort.toCommentDto(),
                page = page,
                unreadOnly = unreadOnly,
            )
            val dto = response.body() ?: return@runCatching emptyList()
            dto.mentions.map { it.toModel() }
        }.getOrNull()
    }

    override suspend fun getReplies(
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
        unreadOnly: Boolean,
    ): List<PersonMentionModel>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = services.user.getReplies(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                limit = limit,
                sort = sort.toCommentDto(),
                page = page,
                unreadOnly = unreadOnly,
            )
            val dto = response.body() ?: return@runCatching emptyList()
            dto.replies.map { it.toModel() }
        }.getOrNull()
    }

    override suspend fun readAll(
        auth: String?,
    ): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val data = MarkAllAsReadForm(auth.orEmpty())
            services.user.markAllAsRead(
                authHeader = auth.toAuthHeader(),
                form = data,
            )
        }
    }

    override suspend fun setMentionRead(read: Boolean, mentionId: Long, auth: String?): Unit =
        withContext(Dispatchers.IO) {
            runCatching {
                val data = MarkPersonMentionAsReadForm(
                    mentionId = mentionId,
                    read = read,
                    auth = auth.orEmpty(),
                )
                services.user.markPersonMentionAsRead(
                    authHeader = auth.toAuthHeader(),
                    form = data,
                )
            }.getOrElse { }
        }

    override suspend fun setReplyRead(read: Boolean, replyId: Long, auth: String?): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val data = MarkCommentAsReadForm(
                replyId = replyId,
                read = read,
                auth = auth.orEmpty(),
            )
            services.comment.markAsRead(
                authHeader = auth.toAuthHeader(),
                form = data,
            )
        }.getOrElse { }
    }

    override suspend fun block(id: Long, blocked: Boolean, auth: String?): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val data = BlockPersonForm(
                personId = id,
                block = blocked,
                auth = auth.orEmpty(),
            )
            services.user.block(
                authHeader = auth.toAuthHeader(),
                form = data,
            )
            Unit
        }
    }

    override suspend fun getModeratedCommunities(
        auth: String?,
        id: Long?,
    ): List<CommunityModel> = withContext(Dispatchers.IO) {
        runCatching {
            val response = services.user.getDetails(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                personId = id,
            ).body()
            response?.moderates?.map {
                it.community.toModel()
            }.orEmpty()
        }.getOrElse { emptyList() }
    }

    override suspend fun getLikedPosts(
        auth: String?,
        page: Int,
        pageCursor: String?,
        limit: Int,
        sort: SortType,
        liked: Boolean,
    ): Pair<List<PostModel>, String?>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = services.post.getAll(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                page = page,
                pageCursor = pageCursor,
                limit = limit,
                sort = sort.toDto(),
                type = ListingType.All,
                likedOnly = if (liked) true else null,
                dislikedOnly = if (!liked) true else null,
            )
            val body = response.body()
            val posts = body?.posts?.map { it.toModel() } ?: emptyList()
            posts to body?.nextPage
        }.getOrNull()
    }

    override suspend fun getLikedComments(
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
        liked: Boolean,
    ): List<CommentModel>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = services.comment.getAll(
                authHeader = auth.toAuthHeader(),
                auth = auth,
                page = page,
                limit = limit,
                sort = sort.toCommentDto(),
                type = ListingType.All,
                likedOnly = if (liked) true else null,
                dislikedOnly = if (!liked) true else null,
            ).body()
            response?.comments?.map { it.toModel() }
        }.getOrElse { emptyList() }
    }

    override suspend fun purge(auth: String?, id: Long, reason: String?) = withContext(Dispatchers.IO) {
        val data = PurgePersonForm(
            personId = id,
            reason = reason,
        )
        val response = services.user.purge(
            form = data,
            authHeader = auth.toAuthHeader(),
        )
        require(response.body()?.success == true)
    }
}
