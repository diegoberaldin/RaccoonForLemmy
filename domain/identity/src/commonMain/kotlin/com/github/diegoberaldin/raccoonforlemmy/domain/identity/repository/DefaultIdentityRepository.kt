package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class DefaultIdentityRepository(
    private val accountRepository: AccountRepository,
    private val siteRepository: SiteRepository,
) : IdentityRepository {

    private val scope = CoroutineScope(SupervisorJob())
    override val authToken = MutableStateFlow<String?>(null)

    override val isLogged = authToken.map { authOrNull ->
        if (authOrNull.isNullOrEmpty()) {
            false
        } else {
            val currentUser = siteRepository.getCurrentUser(authOrNull)
            currentUser != null
        }
    }.stateIn(
        scope = scope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed(5_000)
    )

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
