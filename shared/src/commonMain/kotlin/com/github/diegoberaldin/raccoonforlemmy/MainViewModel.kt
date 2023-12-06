package com.github.diegoberaldin.raccoonforlemmy

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(
    private val mvi: DefaultMviModel<MainScreenMviModel.Intent, MainScreenMviModel.UiState, MainScreenMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
    private val messageRepository: PrivateMessageRepository,
) : MainScreenMviModel,
    MviModel<MainScreenMviModel.Intent, MainScreenMviModel.UiState, MainScreenMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()

        mvi.scope?.launch(Dispatchers.IO) {
            launch {
                identityRepository.isLogged.onEach { logged ->
                    val unreadCount = if (logged == true) {
                        val auth = identityRepository.authToken.value
                        val mentionCount =
                            userRepository.getMentions(auth, page = 1, limit = 50).orEmpty().count()
                        val replyCount =
                            userRepository.getReplies(auth, page = 1, limit = 50).orEmpty().count()
                        val messageCount =
                            messageRepository.getAll(auth, page = 1, limit = 50).orEmpty().groupBy {
                                listOf(it.creator?.id ?: 0, it.recipient?.id ?: 0).sorted()
                                    .joinToString()
                            }.count()
                        mentionCount + replyCount + messageCount
                    } else {
                        0
                    }
                    mvi.emitEffect(MainScreenMviModel.Effect.UnreadItemsDetected(unreadCount))
                }.launchIn(this)
            }

        }
    }

    override fun reduce(intent: MainScreenMviModel.Intent) {
        when (intent) {
            is MainScreenMviModel.Intent.SetBottomBarOffsetHeightPx -> {
                mvi.updateState { it.copy(bottomBarOffsetHeightPx = intent.value) }
            }
        }
    }
}