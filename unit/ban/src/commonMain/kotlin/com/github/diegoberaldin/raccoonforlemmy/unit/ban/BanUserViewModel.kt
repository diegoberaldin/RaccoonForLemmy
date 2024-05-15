package com.github.diegoberaldin.raccoonforlemmy.unit.ban

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import kotlinx.coroutines.launch

class BanUserViewModel(
    private val userId: Long,
    private val communityId: Long,
    private val newValue: Boolean,
    private val postId: Long?,
    private val commentId: Long?,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val notificationCenter: NotificationCenter,
) : BanUserMviModel,
    DefaultMviModel<BanUserMviModel.Intent, BanUserMviModel.UiState, BanUserMviModel.Effect>(
        initialState = BanUserMviModel.UiState(),
    ) {

        init {
            updateState {
                it.copy(targetBanValue = newValue)
            }
        }

        override fun reduce(intent: BanUserMviModel.Intent) {
            when (intent) {
                BanUserMviModel.Intent.IncrementDays -> incrementDays()
                BanUserMviModel.Intent.DecrementDays -> decrementDays()
                is BanUserMviModel.Intent.ChangePermanent -> updateState { it.copy(permanent = intent.value) }
                is BanUserMviModel.Intent.ChangeRemoveData -> updateState { it.copy(removeData = intent.value) }
                is BanUserMviModel.Intent.SetText -> updateState { it.copy(text = intent.value) }
                BanUserMviModel.Intent.Submit -> submit()
            }
        }

        private fun incrementDays() {
            val newValue = uiState.value.days + 1
            updateState { it.copy(days = newValue) }
        }

        private fun decrementDays() {
            val newValue = (uiState.value.days - 1).coerceAtLeast(1)
            updateState { it.copy(days = newValue) }
        }

        private fun submit() {
            val currentState = uiState.value
            if (currentState.loading) {
                return
            }
            val text = currentState.text
            val removeData = currentState.removeData.takeIf { newValue } ?: false
            val days = currentState.days.toLong().takeIf { newValue }

            updateState { it.copy(loading = true) }
            screenModelScope.launch {
                try {
                    val auth = identityRepository.authToken.value.orEmpty()
                    val newUser = communityRepository.banUser(
                        auth = auth,
                        userId = userId,
                        communityId = communityId,
                        ban = newValue,
                        expires = days,
                        reason = text,
                        removeData = removeData,
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
