package com.github.diegoberaldin.raccoonforlemmy.domain.inbox.usecase

import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository

internal class DefaultGetUnreadItemsUseCase(
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
    private val messageRepository: PrivateMessageRepository,
) : GetUnreadItemsUseCase {
    override suspend fun getUnreadReplies(): Int {
        val auth = identityRepository.authToken.value
        return userRepository.getReplies(auth, page = 1, limit = 50).orEmpty().count()
    }

    override suspend fun getUnreadMentions(): Int {
        val auth = identityRepository.authToken.value
        return userRepository.getMentions(auth, page = 1, limit = 50).orEmpty().count()
    }

    override suspend fun getUnreadMessages(): Int {
        val auth = identityRepository.authToken.value
        return messageRepository.getAll(auth, page = 1, limit = 50).orEmpty().groupBy {
            listOf(it.creator?.id ?: 0, it.recipient?.id ?: 0).sorted()
                .joinToString()
        }.count()
    }
}
