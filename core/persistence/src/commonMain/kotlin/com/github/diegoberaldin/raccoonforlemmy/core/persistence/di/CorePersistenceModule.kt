package com.github.diegoberaldin.raccoonforlemmy.core.persistence.di

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DefaultDatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.CommunitySortRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DefaultAccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DefaultCommunitySortRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DefaultDraftRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DefaultFavoriteCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DefaultInstanceSelectionRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DefaultMultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DefaultSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DraftRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.FavoriteCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.InstanceSelectionRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.usecase.DefaultExportSettingsUseCase
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.usecase.DefaultImportSettingsUseCase
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.usecase.ExportSettingsUseCase
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.usecase.ImportSettingsUseCase
import org.koin.dsl.module

val corePersistenceModule = module {
    includes(persistenceInnerModule)
    single<DatabaseProvider> {
        DefaultDatabaseProvider(
            driverFactory = get(),
        )
    }
    single<AccountRepository> {
        DefaultAccountRepository(
            provider = get(),
        )
    }
    single<SettingsRepository> {
        DefaultSettingsRepository(
            provider = get(),
            keyStore = get(),
        )
    }
    single<MultiCommunityRepository> {
        DefaultMultiCommunityRepository(
            provider = get(),
        )
    }
    single<FavoriteCommunityRepository> {
        DefaultFavoriteCommunityRepository(
            provider = get(),
        )
    }
    single<InstanceSelectionRepository> {
        DefaultInstanceSelectionRepository(
            keyStore = get(),
        )
    }
    single<DraftRepository> {
        DefaultDraftRepository(
            provider = get(),
        )
    }
    single<CommunitySortRepository> {
        DefaultCommunitySortRepository(
            keyStore = get(),
        )
    }
    single<ImportSettingsUseCase> {
        DefaultImportSettingsUseCase(
            settingsRepository = get(),
            accountRepository = get(),
        )
    }
    single<ExportSettingsUseCase> {
        DefaultExportSettingsUseCase(
            settingsRepository = get(),
        )
    }
}
