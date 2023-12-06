package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.usecase

interface GetUnreadItemsUseCase {
    suspend fun getUnreadReplies(): Int
    suspend fun getUnreadMentions(): Int
    suspend fun getUnreadMessages(): Int
}
