package com.github.diegoberaldin.raccoonforlemmy.domain.identity.di

import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.AuthRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.DefaultApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.DefaultAuthRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.DefaultIdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.DefaultDeleteAccountUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.DefaultLoginUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.DefaultLogoutUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.DefaultSwitchAccountUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.DeleteAccountUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.LoginUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.LogoutUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.SwitchAccountUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreIdentityModule = module {
    single<ApiConfigurationRepository> {
        DefaultApiConfigurationRepository(
            serviceProvider = get(named("default")),
            keyStore = get(),
        )
    }
    single<IdentityRepository> {
        DefaultIdentityRepository(
            accountRepository = get(),
            siteRepository = get(),
            networkManager = get(),
        )
    }
    single<AuthRepository> {
        DefaultAuthRepository(
            services = get(named("default")),
        )
    }
    single<LoginUseCase> {
        DefaultLoginUseCase(
            apiConfigurationRepository = get(),
            authRepository = get(),
            identityRepository = get(),
            accountRepository = get(),
            settingsRepository = get(),
            siteRepository = get(),
        )
    }
    single<LogoutUseCase> {
        DefaultLogoutUseCase(
            identityRepository = get(),
            accountRepository = get(),
            notificationCenter = get(),
            settingsRepository = get(),
            contentResetCoordinator = get(),
        )
    }
    single<SwitchAccountUseCase> {
        DefaultSwitchAccountUseCase(
            identityRepository = get(),
            accountRepository = get(),
            settingsRepository = get(),
            serviceProvider = get(named("default")),
            notificationCenter = get(),
        )
    }
    single<DeleteAccountUseCase> {
        DefaultDeleteAccountUseCase(
            accountRepository = get(),
        )
    }
}
