package com.github.diegoberaldin.raccoonforlemmy.core.persistence.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString

interface ExportSettingsUseCase {
    suspend operator fun invoke(): String
}

internal class DefaultExportSettingsUseCase(
    private val settingsRepository: SettingsRepository,
) : ExportSettingsUseCase {
    override suspend fun invoke(): String =
        withContext(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value
            val data = settings.toData()
            jsonSerializationStrategy.encodeToString(data)
        }
}
