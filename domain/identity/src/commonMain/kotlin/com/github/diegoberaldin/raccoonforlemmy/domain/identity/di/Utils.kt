package com.github.diegoberaldin.raccoonforlemmy.domain.identity.di

import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository

expect fun getApiConfigurationRepository(): ApiConfigurationRepository
