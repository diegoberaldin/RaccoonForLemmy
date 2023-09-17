package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkAllAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkCommentReplyAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPersonMentionAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toCommentDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toHost
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

class UserRepository(
    private val serviceProvider: ServiceProvider,
) {

    suspend fun get(
        id: Int,
        auth: String? = null,
    ): UserModel? = runCatching {
        val response = serviceProvider.user.getDetails(
            auth = auth,
            personId = id,
        )
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

    suspend fun getPosts(
        id: Int,
        auth: String? = null,
        page: Int,
        limit: Int = PostsRepository.DEFAULT_PAGE_SIZE,
        sort: SortType = SortType.Active,
    ): List<PostModel> = runCatching {
        val response = serviceProvider.user.getDetails(
            auth = auth,
            personId = id,
            page = page,
            limit = limit,
            sort = sort.toCommentDto(),
        )
        val dto = response.body() ?: return@runCatching emptyList()
        dto.posts.map { it.toModel() }
    }.getOrElse { emptyList() }

    suspend fun getSavedPosts(
        id: Int,
        auth: String? = null,
        page: Int,
        limit: Int = PostsRepository.DEFAULT_PAGE_SIZE,
        sort: SortType = SortType.Active,
    ): List<PostModel> = runCatching {
        val response = serviceProvider.user.getDetails(
            auth = auth,
            personId = id,
            page = page,
            limit = limit,
            sort = sort.toCommentDto(),
            savedOnly = true,
        )
        val dto = response.body() ?: return@runCatching emptyList()
        dto.posts.map { it.toModel() }
    }.getOrElse { emptyList() }

    suspend fun getComments(
        id: Int,
        auth: String? = null,
        page: Int,
        limit: Int = PostsRepository.DEFAULT_PAGE_SIZE,
        sort: SortType = SortType.Active,
    ): List<CommentModel> = runCatching {
        val response = serviceProvider.user.getDetails(
            auth = auth,
            personId = id,
            page = page,
            limit = limit,
            sort = sort.toCommentDto(),
        )
        val dto = response.body() ?: return@runCatching emptyList()
        dto.comments.map { it.toModel() }
    }.getOrElse { emptyList() }

    suspend fun getSavedComments(
        id: Int,
        auth: String? = null,
        page: Int,
        limit: Int = PostsRepository.DEFAULT_PAGE_SIZE,
        sort: SortType = SortType.Active,
    ): List<CommentModel> = runCatching {
        val response = serviceProvider.user.getDetails(
            auth = auth,
            personId = id,
            page = page,
            limit = limit,
            sort = sort.toCommentDto(),
            savedOnly = true,
        )
        val dto = response.body() ?: return@runCatching emptyList()
        dto.comments.map { it.toModel() }
    }.getOrElse { emptyList() }

    suspend fun getMentions(
        auth: String? = null,
        page: Int,
        limit: Int = PostsRepository.DEFAULT_PAGE_SIZE,
        sort: SortType = SortType.Active,
        unreadOnly: Boolean = true,
    ): List<PersonMentionModel> = runCatching {
        val response = serviceProvider.user.getMentions(
            auth = auth,
            limit = limit,
            sort = sort.toCommentDto(),
            page = page,
            unreadOnly = unreadOnly,
        )
        val dto = response.body() ?: return@runCatching emptyList()
        dto.mentions.map { it.toModel() }
    }.getOrElse { emptyList() }

    suspend fun getReplies(
        auth: String? = null,
        page: Int,
        limit: Int = PostsRepository.DEFAULT_PAGE_SIZE,
        sort: SortType = SortType.Active,
        unreadOnly: Boolean = true,
    ): List<PersonMentionModel> = runCatching {
        val response = serviceProvider.user.getReplies(
            auth = auth,
            limit = limit,
            sort = sort.toCommentDto(),
            page = page,
            unreadOnly = unreadOnly,
        )
        val dto = response.body() ?: return@runCatching emptyList()
        dto.replies.map { it.toModel() }
    }.getOrElse { emptyList() }

    suspend fun readAll(
        auth: String? = null,
    ) {
        val data = MarkAllAsReadForm(auth.orEmpty())
        serviceProvider.user.markAllAsRead(data)
    }

    suspend fun setMentionRead(read: Boolean, mentionId: Int, auth: String? = null) = runCatching {
        val data = MarkPersonMentionAsReadForm(
            mentionId = mentionId,
            read = read,
            auth = auth.orEmpty(),
        )
        serviceProvider.user.markPersonMentionAsRead(data)
    }

    suspend fun setReplyRead(read: Boolean, replyId: Int, auth: String? = null) = runCatching {
        val data = MarkCommentReplyAsReadForm(
            replyId = replyId,
            read = read,
            auth = auth.orEmpty(),
        )
        serviceProvider.user.markCommentReplyAsRead(data)
    }
}
