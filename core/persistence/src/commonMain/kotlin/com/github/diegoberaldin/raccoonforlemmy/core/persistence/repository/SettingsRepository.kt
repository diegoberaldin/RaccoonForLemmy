package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import androidx.compose.runtime.Stable
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import kotlinx.coroutines.flow.StateFlow

@Stable
interface SettingsRepository {

    val currentSettings: StateFlow<SettingsModel>

    suspend fun createSettings(settings: SettingsModel, accountId: Long)

    suspend fun getSettings(accountId: Long?): SettingsModel

    suspend fun updateSettings(settings: SettingsModel, accountId: Long?)

    fun changeCurrentSettings(settings: SettingsModel)
}
