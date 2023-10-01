package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {

    val currentSettings: StateFlow<SettingsModel>

    suspend fun createSettings(settings: SettingsModel, accountId: Long)

    suspend fun getSettings(accountId: Long?): SettingsModel

    suspend fun updateSettings(settings: SettingsModel, accountId: Long?)

    fun changeCurrentSettings(settings: SettingsModel)
}
