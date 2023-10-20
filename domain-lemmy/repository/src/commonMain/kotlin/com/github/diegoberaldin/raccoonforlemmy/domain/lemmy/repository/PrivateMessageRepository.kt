package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePrivateMessageForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPrivateMessageAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

class PrivateMessageRepository(
    private val serviceProvider: ServiceProvider,
) {
    suspend fun getAll(
        auth: String? = null,
        page: Int,
        limit: Int = PostRepository.DEFAULT_PAGE_SIZE,
        unreadOnly: Boolean = true,
    ): List<PrivateMessageModel>? = runCatching {
        val response = serviceProvider.privateMessages.getPrivateMessages(
            authHeader = auth.toAuthHeader(),
            auth = auth,
            limit = limit,
            page = page,
            unreadOnly = unreadOnly,
        )
        val dto = response.body() ?: return@runCatching emptyList()
        dto.privateMessages.map { it.toModel() }
    }.getOrNull()

    suspend fun create(
        message: String,
        auth: String? = null,
        recipiendId: Int,
    ) {
        val data = CreatePrivateMessageForm(
            content = message,
            auth = auth.orEmpty(),
            recipientId = recipiendId,
        )
        serviceProvider.privateMessages.createPrivateMessage(
            authHeader = auth.toAuthHeader(),
            form = data,
        )
    }

    suspend fun markAsRead(
        messageId: Int,
        auth: String? = null,
        read: Boolean = true,
    ) {
        val data = MarkPrivateMessageAsReadForm(
            privateMessageId = messageId,
            auth = auth.orEmpty(),
            read = read,
        )
        serviceProvider.privateMessages.markPrivateMessageAsRead(
            authHeader = auth.toAuthHeader(),
            form = data,
        )
    }
}