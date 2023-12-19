package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository

internal class DefaultSwitchAccountUseCase(
    private val identityRepository: IdentityRepository,
    private val accountRepository: AccountRepository,
    private val serviceProvider: ServiceProvider,
    private val notificationCenter: NotificationCenter,
    private val settingsRepository: SettingsRepository,
) : SwitchAccountUseCase {
    override suspend fun invoke(account: AccountModel) {
        val accountId = account.id ?: return
        val jwt = account.jwt.takeIf { it.isNotEmpty() } ?: return
        val instance = account.instance.takeIf { it.isNotEmpty() } ?: return

        val oldActiveAccountId = accountRepository.getActive()?.id
        if (oldActiveAccountId != null) {
            accountRepository.setActive(oldActiveAccountId, false)
        }
        accountRepository.setActive(accountId, true)
        identityRepository.clearToken()
        notificationCenter.send(NotificationCenterEvent.Logout)
        serviceProvider.changeInstance(instance)
        identityRepository.storeToken(jwt)

        val newSettings = settingsRepository.getSettings(accountId)
        settingsRepository.changeCurrentSettings(newSettings)
    }
}