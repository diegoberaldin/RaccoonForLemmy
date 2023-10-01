package com.github.diegoberaldin.raccoonforlemmy.domain.identity.di

import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.AuthRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.DefaultApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.DefaultAuthRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.DefaultIdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.DefaultLoginUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.DefaultLogoutUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.DefaultSwitchAccountUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.LoginUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.LogoutUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.SwitchAccountUseCase
import org.koin.dsl.module

val coreIdentityModule = module {
    single<ApiConfigurationRepository> {
        DefaultApiConfigurationRepository(
            serviceProvider = get(),
        )
    }
    single<IdentityRepository> {
        DefaultIdentityRepository(
            accountRepository = get(),
        )
    }
    single<AuthRepository> {
        DefaultAuthRepository(
            services = get(),
        )
    }
    single<LoginUseCase> {
        DefaultLoginUseCase(
            apiConfigurationRepository = get(),
            authRepository = get(),
            identityRepository = get(),
            accountRepository = get(),
            settingsRepository = get(),
        )
    }
    single<LogoutUseCase> {
        DefaultLogoutUseCase(
            identityRepository = get(),
            accountRepository = get(),
            notificationCenter = get(),
            settingsRepository = get(),
        )
    }
    single<SwitchAccountUseCase> {
        DefaultSwitchAccountUseCase(
            identityRepository = get(),
            accountRepository = get(),
            settingsRepository = get(),
            serviceProvider = get(),
            notificationCenter = get(),
        )
    }
}
