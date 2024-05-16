package com.github.diegoberaldin.raccoonforlemmy.domain.inbox.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DefaultGetUnreadItemsUseCaseTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val identityRepository =
        mockk<IdentityRepository>(relaxUnitFun = true) {
            every { authToken } returns MutableStateFlow("fake-token")
        }
    private val userRepository = mockk<UserRepository>(relaxUnitFun = true)
    private val messageRepository = mockk<PrivateMessageRepository>(relaxUnitFun = true)

    private val sut =
        DefaultGetUnreadItemsUseCase(
            identityRepository = identityRepository,
            userRepository = userRepository,
            messageRepository = messageRepository,
        )

    @Test
    fun whenGetUnreadReplies_thenResultIsAsExpected() =
        runTest {
            coEvery { userRepository.getReplies(any(), any(), any()) } returns listOf(mockk())

            val res = sut.getUnreadReplies()

            assertEquals(1, res)
            coVerify {
                userRepository.getReplies(auth = "fake-token", page = 1, limit = any())
            }
        }

    @Test
    fun whenGetUnreadMentions_thenResultIsAsExpected() =
        runTest {
            coEvery { userRepository.getMentions(any(), any(), any()) } returns listOf(mockk())

            val res = sut.getUnreadMentions()

            assertEquals(1, res)
            coVerify {
                userRepository.getMentions(auth = "fake-token", page = 1, limit = any())
            }
        }

    @Test
    fun whenGetUnreadMessages_thenResultIsAsExpected() =
        runTest {
            val fakeMessage =
                mockk<PrivateMessageModel> {
                    every { creator } returns UserModel(id = 1)
                    every { recipient } returns UserModel(id = 2)
                }
            coEvery { messageRepository.getAll(any(), any(), any()) } returns listOf(fakeMessage)

            val res = sut.getUnreadMessages()

            assertEquals(1, res)
            coVerify {
                messageRepository.getAll(auth = "fake-token", page = 1, limit = any())
            }
        }
}
