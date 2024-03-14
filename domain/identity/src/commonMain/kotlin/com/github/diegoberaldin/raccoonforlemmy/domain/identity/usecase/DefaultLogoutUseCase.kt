package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.CommunitySortRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository

internal class DefaultLogoutUseCase(
    private val identityRepository: IdentityRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
    private val settingsRepository: SettingsRepository,
    private val communitySortRepository: CommunitySortRepository,
) : LogoutUseCase {
    override suspend operator fun invoke() {
        notificationCenter.send(NotificationCenterEvent.ResetExplore)
        notificationCenter.send(NotificationCenterEvent.ResetHome)

        identityRepository.clearToken()
        communitySortRepository.clear()
        notificationCenter.send(NotificationCenterEvent.Logout)

        val oldAccountId = accountRepository.getActive()?.id
        if (oldAccountId != null) {
            accountRepository.setActive(oldAccountId, false)
        }
        val anonSettings = settingsRepository.getSettings(null)
        settingsRepository.changeCurrentSettings(anonSettings)
    }
}
