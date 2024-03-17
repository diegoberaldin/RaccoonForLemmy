package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.network.NetworkManager
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

internal class DefaultIdentityRepository(
    private val accountRepository: AccountRepository,
    private val siteRepository: SiteRepository,
    private val networkManager: NetworkManager,
    private val userRepository: UserRepository,
) : IdentityRepository {

    override val authToken = MutableStateFlow<String?>(null)
    override val isLogged = MutableStateFlow<Boolean?>(null)
    override var cachedUser: UserModel? = null
        private set

    override suspend fun startup() = withContext(Dispatchers.IO) {
        val account = accountRepository.getActive()
        if (account != null) {
            authToken.value = account.jwt
        } else {
            authToken.value = ""
        }
        refreshLoggedState()
    }

    override fun storeToken(jwt: String) {
        authToken.value = jwt
    }

    override fun clearToken() {
        authToken.value = ""
        cachedUser = null
        isLogged.value = false
    }

    override suspend fun refreshLoggedState() = withContext(Dispatchers.IO) {
        val auth = authToken.value.orEmpty()
        if (auth.isEmpty()) {
            isLogged.value = false
        } else {
            val newIsLogged = if (networkManager.isNetworkAvailable()) {
                refreshCachedUser()
                cachedUser != null
            } else {
                null
            }
            isLogged.value = newIsLogged
        }
    }

    private suspend fun refreshCachedUser() {
        val auth = authToken.value.orEmpty()
        val remoteUser = siteRepository.getCurrentUser(auth)?.let { user ->
            val communities = userRepository.getModeratedCommunities(auth, id = user.id)
            user.copy(
                moderator = communities.isNotEmpty()
            )
        }
        cachedUser = remoteUser
    }
}
