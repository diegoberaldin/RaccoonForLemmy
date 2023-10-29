package com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

class ModalDrawerViewModel(
    private val mvi: DefaultMviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val accountRepository: AccountRepository,
    private val multiCommunityRepository: MultiCommunityRepository,
    private val siteRepository: SiteRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val settingsRepository: SettingsRepository,
) : ModalDrawerMviModel,
    MviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch(Dispatchers.Main) {
            apiConfigurationRepository.instance.onEach { instance ->
                mvi.updateState {
                    it.copy(instance = instance)
                }
            }.launchIn(this)
            identityRepository.isLogged.onEach { _ ->
                refreshUser()
                refresh()
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
            }.launchIn(this)
        }
    }

    override fun reduce(intent: ModalDrawerMviModel.Intent) {
        when (intent) {
            ModalDrawerMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
                refresh()
            }

            is ModalDrawerMviModel.Intent.ChangeInstanceName -> mvi.updateState {
                it.copy(changeInstanceName = intent.value)
            }

            ModalDrawerMviModel.Intent.SubmitChangeInstance -> submitChangeInstance()
        }
    }

    private suspend fun refreshUser() {
        val auth = identityRepository.authToken.value.orEmpty()
        if (auth.isEmpty()) {
            mvi.updateState { it.copy(user = null) }
        } else {
            var user = siteRepository.getCurrentUser(auth)
            runCatching {
                withTimeout(2000) {
                    while (user == null) {
                        // retry getting user if non-empty auth
                        delay(500)
                        user = siteRepository.getCurrentUser(auth)
                        yield()
                    }
                    mvi.updateState { it.copy(user = user) }
                }
            }
        }
    }

    private suspend fun refresh() {
        if (uiState.value.refreshing) {
            return
        }
        mvi.updateState { it.copy(refreshing = true) }

        val auth = identityRepository.authToken.value
        val communities = communityRepository.getSubscribed(auth).sortedBy { it.name }
        val accountId = accountRepository.getActive()?.id ?: 0L
        val multiCommunitites = multiCommunityRepository.getAll(accountId).sortedBy { it.name }

        mvi.updateState {
            it.copy(
                refreshing = false,
                communities = communities,
                multiCommunities = multiCommunitites,
            )
        }
    }

    private fun submitChangeInstance() {
        mvi.updateState { it.copy(changeInstanceNameError = null) }
        var valid = true
        val instanceName = uiState.value.changeInstanceName
        if (instanceName.isEmpty()) {
            mvi.updateState { it.copy(changeInstanceNameError = MR.strings.message_missing_field.desc()) }
            valid = false
        }
        if (!valid) {
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(changeInstanceloading = true) }
            val res = communityRepository.getAll(
                instance = instanceName,
                page = 1,
                limit = 1
            ) ?: emptyList()
            if (res.isEmpty()) {
                mvi.updateState {
                    it.copy(
                        changeInstanceNameError = MR.strings.message_invalid_field.desc(),
                        changeInstanceloading = false,
                    )
                }
                return@launch
            }

            apiConfigurationRepository.changeInstance(instanceName)
            mvi.updateState {
                it.copy(
                    changeInstanceloading = false,
                    changeInstanceName = "",
                )
            }
            mvi.emitEffect(ModalDrawerMviModel.Effect.CloseChangeInstanceDialog)
        }
    }
}