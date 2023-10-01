package com.github.diegoberaldin.raccoonforlemmy.core.persistence.di

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import org.koin.core.module.Module

internal expect val persistenceInnerModule: Module

expect fun getAccountRepository(): AccountRepository

expect fun getSettingsRepository(): SettingsRepository
