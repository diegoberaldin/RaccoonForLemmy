package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository

internal class DefaultLogoutUseCase(
    private val identityRepository: IdentityRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
    private val settingsRepository: SettingsRepository,
) : LogoutUseCase {
    override suspend operator fun invoke() {
        identityRepository.clearToken()
        notificationCenter.send(NotificationCenterEvent.Logout)
        notificationCenter.send(NotificationCenterEvent.ResetContents)
        val oldAccountId = accountRepository.getActive()?.id
        if (oldAccountId != null) {
            accountRepository.setActive(oldAccountId, false)
        }
        val anonSettings = settingsRepository.getSettings(null)
        settingsRepository.changeCurrentSettings(anonSettings)
    }
}
