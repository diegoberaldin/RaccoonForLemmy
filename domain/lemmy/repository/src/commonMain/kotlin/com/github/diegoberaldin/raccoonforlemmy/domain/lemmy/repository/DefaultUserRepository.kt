package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockPersonForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkAllAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkCommentAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPersonMentionAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toCommentDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toHost
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

internal class DefaultUserRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) : UserRepository {

    override suspend fun get(
        id: Int,
        auth: String?,
        username: String?,
        otherInstance: String?,
    ): UserModel? = runCatching {
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
        UserModel(
            id = dto.personView.person.id,
            name = dto.personView.person.name,
            avatar = dto.personView.person.avatar,
            banner = dto.personView.person.banner,
            host = dto.personView.person.actorId.toHost(),
            score = dto.personView.counts.toModel(),
            accountAge = dto.personView.person.published,
        )
    }.getOrNull()

    override suspend fun getPosts(
        id: Int,
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
        username: String?,
        otherInstance: String?,
    ): List<PostModel>? = runCatching {
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

    override suspend fun getSavedPosts(
        id: Int,
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
    ): List<PostModel>? = runCatching {
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

    override suspend fun getComments(
        id: Int,
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
        username: String?,
        otherInstance: String?,
    ): List<CommentModel>? = runCatching {
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

    override suspend fun getSavedComments(
        id: Int,
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
    ): List<CommentModel>? = runCatching {
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

    override suspend fun getMentions(
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
        unreadOnly: Boolean,
    ): List<PersonMentionModel>? = runCatching {
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

    override suspend fun getReplies(
        auth: String?,
        page: Int,
        limit: Int,
        sort: SortType,
        unreadOnly: Boolean,
    ): List<PersonMentionModel>? = runCatching {
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

    override suspend fun readAll(
        auth: String?,
    ) {
        val data = MarkAllAsReadForm(auth.orEmpty())
        services.user.markAllAsRead(
            authHeader = auth.toAuthHeader(),
            form = data,
        )
    }

    override suspend fun setMentionRead(read: Boolean, mentionId: Int, auth: String?) =
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
        }

    override suspend fun setReplyRead(read: Boolean, replyId: Int, auth: String?) = runCatching {
        val data = MarkCommentAsReadForm(
            replyId = replyId,
            read = read,
            auth = auth.orEmpty(),
        )
        services.comment.markAsRead(
            authHeader = auth.toAuthHeader(),
            form = data,
        )
    }

    override suspend fun block(id: Int, blocked: Boolean, auth: String?): Result<Unit> =
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
        }
}
