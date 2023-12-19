package com.github.diegoberaldin.raccoonforlemmy.domain.identity.di

import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import org.koin.java.KoinJavaComponent.inject

actual fun getApiConfigurationRepository(): ApiConfigurationRepository {
    val res: ApiConfigurationRepository by inject(ApiConfigurationRepository::class.java)
    return res
}
