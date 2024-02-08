package com.github.diegoberaldin.raccoonforlemmy.unit.ban

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class BanUserViewModel(
    private val userId: Int,
    private val communityId: Int,
    private val newValue: Boolean,
    private val postId: Int?,
    private val commentId: Int?,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val notificationCenter: NotificationCenter,
) : BanUserMviModel,
    DefaultMviModel<BanUserMviModel.Intent, BanUserMviModel.UiState, BanUserMviModel.Effect>(
        initialState = BanUserMviModel.UiState(),
    ) {

    override fun reduce(intent: BanUserMviModel.Intent) {
        when (intent) {
            is BanUserMviModel.Intent.SetText -> {
                updateState {
                    it.copy(text = intent.value)
                }
            }

            BanUserMviModel.Intent.Submit -> submit()
        }
    }

    private fun submit() {
        if (uiState.value.loading) {
            return
        }
        val text = uiState.value.text

        updateState { it.copy(loading = true) }
        scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                val newUser = communityRepository.banUser(
                    auth = auth,
                    userId = userId,
                    communityId = communityId,
                    ban = newValue,
                    reason = text,
                    removeData = false,
                )
                if (newUser != null) {
                    postId?.also {
                        val evt = NotificationCenterEvent.UserBannedPost(
                            postId = it,
                            user = newUser,
                        )
                        notificationCenter.send(evt)
                    }
                    commentId?.also {
                        val evt = NotificationCenterEvent.UserBannedComment(
                            commentId = it,
                            user = newUser,
                        )
                        notificationCenter.send(evt)
                    }
                }
                emitEffect(BanUserMviModel.Effect.Success)
            } catch (e: Throwable) {
                val message = e.message
                emitEffect(BanUserMviModel.Effect.Failure(message))
            } finally {
                updateState { it.copy(loading = false) }
            }
        }
    }
}