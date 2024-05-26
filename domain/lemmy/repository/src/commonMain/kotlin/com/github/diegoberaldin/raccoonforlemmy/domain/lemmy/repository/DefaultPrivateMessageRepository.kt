package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePrivateMessageForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeletePrivateMessageForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditPrivateMessageForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPrivateMessageAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultPrivateMessageRepository(
    private val services: ServiceProvider,
) : PrivateMessageRepository {
    override suspend fun getAll(
        auth: String?,
        creatorId: Long?,
        page: Int,
        limit: Int,
        unreadOnly: Boolean,
    ): List<PrivateMessageModel>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.privateMessages.getAll(
                        authHeader = auth.toAuthHeader(),
                        auth = auth,
                        creatorId = creatorId,
                        limit = limit,
                        page = page,
                        unreadOnly = unreadOnly,
                    )
                response.privateMessages.map { it.toModel() }
            }.getOrNull()
        }

    override suspend fun create(
        message: String,
        auth: String?,
        recipientId: Long,
    ): PrivateMessageModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    CreatePrivateMessageForm(
                        content = message,
                        auth = auth.orEmpty(),
                        recipientId = recipientId,
                    )
                val response =
                    services.privateMessages.create(
                        authHeader = auth.toAuthHeader(),
                        form = data,
                    )
                response.privateMessageView.toModel()
            }.getOrNull()
        }

    override suspend fun edit(
        messageId: Long,
        message: String,
        auth: String?,
    ): PrivateMessageModel? =
        runCatching {
            val data =
                EditPrivateMessageForm(
                    content = message,
                    auth = auth.orEmpty(),
                    privateMessageId = messageId,
                )
            val response =
                services.privateMessages.edit(
                    authHeader = auth.toAuthHeader(),
                    form = data,
                )
            response.privateMessageView.toModel()
        }.getOrNull()

    override suspend fun markAsRead(
        messageId: Long,
        auth: String?,
        read: Boolean,
    ): PrivateMessageModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    MarkPrivateMessageAsReadForm(
                        privateMessageId = messageId,
                        auth = auth.orEmpty(),
                        read = read,
                    )
                val response =
                    services.privateMessages.markAsRead(
                        authHeader = auth.toAuthHeader(),
                        form = data,
                    )
                response.privateMessageView.toModel()
            }.getOrNull()
        }

    override suspend fun delete(
        messageId: Long,
        auth: String?,
    ): Unit =
        withContext(Dispatchers.IO) {
            runCatching {
                val data =
                    DeletePrivateMessageForm(
                        auth = auth.orEmpty(),
                        privateMessageId = messageId,
                        deleted = true,
                    )
                services.privateMessages.delete(
                    authHeader = auth.toAuthHeader(),
                    form = data,
                )
            }.getOrDefault(Unit)
        }
}
