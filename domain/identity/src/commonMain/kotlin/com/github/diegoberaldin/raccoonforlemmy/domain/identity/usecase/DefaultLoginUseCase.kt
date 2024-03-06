package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.logDebug
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.AuthRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository

internal class DefaultLoginUseCase(
    private val authRepository: AuthRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val identityRepository: IdentityRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val siteRepository: SiteRepository,
) : LoginUseCase {

    override suspend operator fun invoke(
        instance: String,
        username: String,
        password: String,
        totp2faToken: String?,
    ): Result<Unit> {
        val oldInstance = apiConfigurationRepository.instance.value
        apiConfigurationRepository.changeInstance(instance)

        val response = authRepository.login(
            username = username,
            password = password,
            totp2faToken = totp2faToken,
        )
        return response.onFailure {
            logDebug("Login failure: ${it.message}")
        }.map {
            val auth = it.token
            if (auth == null) {
                apiConfigurationRepository.changeInstance(oldInstance)
            } else {
                val accountSettings = siteRepository.getAccountSettings(auth)
                identityRepository.storeToken(auth)
                identityRepository.refreshLoggedState()

                val account = AccountModel(
                    username = username,
                    instance = instance,
                    jwt = auth
                )
                val existingId = accountRepository.getBy(username, instance)?.id
                val id = existingId ?: run {
                    // new account with a copy of the anonymous settings
                    // (except a couple of fields from the Lemmy accounts)
                    val res = accountRepository.createAccount(account)
                    val anonymousSettings = settingsRepository.getSettings(null)
                        .copy(
                            showScores = accountSettings?.showScores ?: true,
                            includeNsfw = accountSettings?.showNsfw ?: false,
                        )
                    settingsRepository.createSettings(
                        settings = anonymousSettings,
                        accountId = res,
                    )
                    res
                }
                val oldActiveAccountId = accountRepository.getActive()?.id
                if (oldActiveAccountId != null) {
                    accountRepository.setActive(oldActiveAccountId, false)
                }
                accountRepository.setActive(id, true)

                val newSettings = settingsRepository.getSettings(id)
                settingsRepository.changeCurrentSettings(newSettings)
            }
        }
    }
}
