package com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.SwitchAccountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageAccountsViewModel(
    private val mvi: DefaultMviModel<ManageAccountsMviModel.Intent, ManageAccountsMviModel.UiState, ManageAccountsMviModel.Effect>,
    private val accountRepository: AccountRepository,
    private val switchAccount: SwitchAccountUseCase,
) : ScreenModel,
    MviModel<ManageAccountsMviModel.Intent, ManageAccountsMviModel.UiState, ManageAccountsMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()

        if (uiState.value.accounts.isEmpty()) {
            mvi.scope?.launch {
                val accounts = accountRepository.getAll()
                mvi.updateState { it.copy(accounts = accounts) }
            }
        }
    }

    override fun reduce(intent: ManageAccountsMviModel.Intent) {
        when (intent) {
            is ManageAccountsMviModel.Intent.SwitchAccount -> handleSwitchAccount(
                account = uiState.value.accounts[intent.index]
            )
        }
    }

    private fun handleSwitchAccount(account: AccountModel) {
        if (account.active) {
            return
        }
        mvi.scope?.launch(Dispatchers.IO) {
            switchAccount(account)
            withContext(Dispatchers.Main) {
                mvi.emitEffect(ManageAccountsMviModel.Effect.Close)
            }
        }
    }
}