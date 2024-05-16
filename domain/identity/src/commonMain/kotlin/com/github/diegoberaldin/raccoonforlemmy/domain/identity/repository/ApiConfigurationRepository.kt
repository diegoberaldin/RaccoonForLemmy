package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

@Stable
interface ApiConfigurationRepository {
    val instance: StateFlow<String>

    fun changeInstance(value: String)
}
