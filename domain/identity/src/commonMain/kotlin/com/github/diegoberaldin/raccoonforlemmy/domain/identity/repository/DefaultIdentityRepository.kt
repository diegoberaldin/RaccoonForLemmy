package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.network.NetworkManager
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class DefaultIdentityRepository(
    private val accountRepository: AccountRepository,
    private val siteRepository: SiteRepository,
    private val networkManager: NetworkManager,
) : IdentityRepository {

    private val scope = CoroutineScope(SupervisorJob())
    override val authToken = MutableStateFlow<String?>(null)
    override val isLogged = MutableStateFlow<Boolean?>(null)

    init {
        scope.launch {
            val account = accountRepository.getActive()
            if (account != null) {
                authToken.value = account.jwt
            } else {
                authToken.value = ""
            }
            refreshLoggedState()
        }
    }

    override fun storeToken(jwt: String) {
        authToken.value = jwt
        refreshLoggedState()
    }

    override fun clearToken() {
        authToken.value = ""
        isLogged.value = false
    }

    override fun refreshLoggedState() {
        scope.launch(Dispatchers.IO) {
            val auth = authToken.value.orEmpty()
            if (auth.isEmpty()) {
                isLogged.value = false
            } else {
                val newIsLogged = if (networkManager.isNetworkAvailable()) {
                    val currentUser = siteRepository.getCurrentUser(auth)
                    currentUser != null
                } else {
                    null
                }
                isLogged.value = newIsLogged
            }
        }
    }
}
