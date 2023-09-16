package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostLikeForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeletePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SavePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

class PostsRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) {

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }

    suspend fun getAll(
        auth: String? = null,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.Local,
        sort: SortType = SortType.Active,
        communityId: Int? = null,
    ): List<PostModel> = runCatching {
        val response = services.post.getAll(
            auth = auth,
            communityId = communityId,
            page = page,
            limit = limit,
            type = type.toDto(),
            sort = sort.toDto(),
        )
        val dto = response.body()?.posts ?: emptyList()
        dto.map { it.toModel() }
    }.getOrElse { emptyList() }

    suspend fun getAllInInstance(
        instance: String = "",
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        type: ListingType = ListingType.Local,
        sort: SortType = SortType.Active,
        communityId: Int? = null,
    ): List<PostModel> = runCatching {
        customServices.changeInstance(instance)
        val response = customServices.post.getAll(
            communityId = communityId,
            page = page,
            limit = limit,
            type = type.toDto(),
            sort = sort.toDto(),
        )
        val dto = response.body()?.posts ?: emptyList()
        dto.map { it.toModel() }
    }.getOrElse { emptyList() }

    fun asUpVoted(post: PostModel, voted: Boolean) = post.copy(
        myVote = if (voted) 1 else 0,
        score = when {
            voted && post.myVote < 0 -> post.score + 2
            voted -> post.score + 1
            !voted -> post.score - 1
            else -> post.score
        },
    )

    suspend fun upVote(post: PostModel, auth: String, voted: Boolean) = runCatching {
        val data = CreatePostLikeForm(
            postId = post.id,
            score = if (voted) 1 else 0,
            auth = auth,
        )
        services.post.like(data)
    }

    fun asDownVoted(post: PostModel, downVoted: Boolean) = post.copy(
        myVote = if (downVoted) -1 else 0,
        score = when {
            downVoted && post.myVote > 0 -> post.score - 2
            downVoted -> post.score - 1
            !downVoted -> post.score + 1
            else -> post.score
        },
    )

    suspend fun downVote(post: PostModel, auth: String, downVoted: Boolean) = runCatching {
        val data = CreatePostLikeForm(
            postId = post.id,
            score = if (downVoted) -1 else 0,
            auth = auth,
        )
        services.post.like(data)
    }

    fun asSaved(post: PostModel, saved: Boolean): PostModel = post.copy(saved = saved)

    suspend fun save(post: PostModel, auth: String, saved: Boolean) = runCatching {
        val data = SavePostForm(
            postId = post.id,
            save = saved,
            auth = auth,
        )
        services.post.save(data)
    }

    suspend fun create(
        communityId: Int,
        title: String,
        body: String?,
        url: String? = null,
        nsfw: Boolean = false,
        auth: String,
    ) {
        val data = CreatePostForm(
            communityId = communityId,
            name = title,
            body = body,
            url = url,
            nsfw = nsfw,
            auth = auth,
        )
        services.post.create(data)
    }

    suspend fun delete(id: Int, auth: String) {
        val data = DeletePostForm(
            postId = id,
            deleted = true,
            auth = auth
        )
        services.post.delete(data)
    }
}
