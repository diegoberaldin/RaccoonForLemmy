package com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.ContentResetCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.DeleteAccountUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.LogoutUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.SwitchAccountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageAccountsViewModel(
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val switchAccount: SwitchAccountUseCase,
    private val logout: LogoutUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val contentResetCoordinator: ContentResetCoordinator,
) : ManageAccountsMviModel,
    DefaultMviModel<ManageAccountsMviModel.Intent, ManageAccountsMviModel.UiState, ManageAccountsMviModel.Effect>(
        initialState = ManageAccountsMviModel.UiState(),
    ) {

    init {
        if (uiState.value.accounts.isEmpty()) {
            screenModelScope.launch {
                settingsRepository.currentSettings.onEach { settings ->
                    updateState {
                        it.copy(
                            autoLoadImages = settings.autoLoadImages,
                            preferNicknames = settings.preferUserNicknames,
                        )
                    }
                }.launchIn(this)

                val accounts = accountRepository.getAll()
                updateState { it.copy(accounts = accounts) }
            }
        }
    }

    override fun reduce(intent: ManageAccountsMviModel.Intent) {
        when (intent) {
            is ManageAccountsMviModel.Intent.SwitchAccount -> {
                uiState.value.accounts.getOrNull(intent.index)?.also { account ->
                    handleSwitchAccount(account)
                }
            }

            is ManageAccountsMviModel.Intent.DeleteAccount -> {
                uiState.value.accounts.getOrNull(intent.index)?.also { account ->
                    if (account.active) {
                        screenModelScope.launch(Dispatchers.IO) {
                            logout()
                            deleteAccount(account)
                            close()
                        }
                    } else {
                        screenModelScope.launch(Dispatchers.IO) {
                            deleteAccount(account)
                            updateState {
                                it.copy(accounts = it.accounts.filter { a -> a.id != account.id })
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleSwitchAccount(account: AccountModel) {
        if (account.active) {
            return
        }
        screenModelScope.launch(Dispatchers.IO) {
            switchAccount(account)
            contentResetCoordinator.resetHome = true
            contentResetCoordinator.resetExplore = true
            contentResetCoordinator.resetInbox = true
            close()
        }
    }

    private suspend fun close() {
        withContext(Dispatchers.Main) {
            emitEffect(ManageAccountsMviModel.Effect.Close)
        }
    }
}