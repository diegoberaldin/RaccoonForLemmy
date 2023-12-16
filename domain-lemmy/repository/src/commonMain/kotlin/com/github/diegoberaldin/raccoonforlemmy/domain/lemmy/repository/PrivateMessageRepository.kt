package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel

interface PrivateMessageRepository {

    companion object {
        private const val DEFAULT_PAGE_SIZE = 50
    }

    suspend fun getAll(
        auth: String? = null,
        page: Int,
        limit: Int = DEFAULT_PAGE_SIZE,
        unreadOnly: Boolean = true,
    ): List<PrivateMessageModel>?

    suspend fun create(
        message: String,
        auth: String? = null,
        recipiendId: Int,
    )

    suspend fun edit(
        messageId: Int,
        message: String,
        auth: String? = null,
    )

    suspend fun markAsRead(
        messageId: Int,
        auth: String? = null,
        read: Boolean = true,
    )
}
