package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive

private const val KEY_LAST_INSTANCE = "lastInstance"

internal class DefaultApiConfigurationRepository(
    private val serviceProvider: ServiceProvider,
    private val keyStore: TemporaryKeyStore,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ApiConfigurationRepository {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

    init {
        val instance =
            keyStore[KEY_LAST_INSTANCE, ""]
                .takeIf { it.isNotEmpty() } ?: serviceProvider.currentInstance
        changeInstance(instance)
    }

    override val instance =
        channelFlow {
            while (isActive) {
                val value = serviceProvider.currentInstance
                trySend(value)
                delay(1_000)
            }
        }.distinctUntilChanged().stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "",
        )

    override fun changeInstance(value: String) {
        serviceProvider.changeInstance(value)
        keyStore.save(KEY_LAST_INSTANCE, value)
    }
}
