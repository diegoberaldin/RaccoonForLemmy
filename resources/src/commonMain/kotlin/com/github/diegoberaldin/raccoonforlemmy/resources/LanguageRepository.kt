package com.github.diegoberaldin.raccoonforlemmy.resources

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

@Stable
interface LanguageRepository {

    val currentLanguage: StateFlow<String>

    fun changeLanguage(lang: String)
}
