package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class DefaultIdentityRepository(
    private val accountRepository: AccountRepository,
) : IdentityRepository {

    private val scope = CoroutineScope(SupervisorJob())
    override val authToken = MutableStateFlow<String?>(null)

    override val isLogged: Flow<Boolean?> = authToken.map {
        it?.isNotEmpty()
    }

    init {
        scope.launch {
            val account = accountRepository.getActive()
            if (account != null) {
                authToken.value = account.jwt
            } else {
                authToken.value = ""
            }
        }
    }

    override fun storeToken(jwt: String) {
        authToken.value = jwt
    }

    override fun clearToken() {
        authToken.value = ""
    }
}
