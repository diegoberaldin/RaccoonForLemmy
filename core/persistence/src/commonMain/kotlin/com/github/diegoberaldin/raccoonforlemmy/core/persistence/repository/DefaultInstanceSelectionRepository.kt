package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

private const val CUSTOM_INSTANCES_KEY = "customInstances"
private val DEFAULT_INSTANCES =
    listOf(
        "lemmy.world",
        "lemmy.ml",
        "lemmy.dbzer0.com",
        "sh.itjust.works",
        "lemm.ee",
        "feddit.de",
        "programming.dev",
        "discuss.tchncs.de",
        "sopuli.xyz",
        "lemmy.blahaj.zone",
        "lemmy.zip",
        "reddthat.com",
        "lemmy.cafe",
    )

internal class DefaultInstanceSelectionRepository(
    private val keyStore: TemporaryKeyStore,
) : InstanceSelectionRepository {
    override suspend fun getAll(): List<String> =
        withContext(Dispatchers.IO) {
            if (!keyStore.containsKey(CUSTOM_INSTANCES_KEY)) {
                keyStore.save(key = CUSTOM_INSTANCES_KEY, value = DEFAULT_INSTANCES)
            }
            keyStore.get(key = CUSTOM_INSTANCES_KEY, default = DEFAULT_INSTANCES)
        }

    override suspend fun add(value: String) =
        withContext(Dispatchers.IO) {
            val oldInstances =
                keyStore.get(
                    key = CUSTOM_INSTANCES_KEY,
                    default = DEFAULT_INSTANCES,
                )
            val instances =
                buildList {
                    if (!oldInstances.contains(value)) {
                        add(value)
                    }
                    addAll(oldInstances)
                }
            keyStore.save(key = CUSTOM_INSTANCES_KEY, value = instances)
        }

    override suspend fun updateAll(values: List<String>) =
        withContext(Dispatchers.IO) {
            keyStore.save(key = CUSTOM_INSTANCES_KEY, value = values)
        }

    override suspend fun remove(value: String) =
        withContext(Dispatchers.IO) {
            val instances =
                keyStore.get(
                    key = CUSTOM_INSTANCES_KEY,
                    default = DEFAULT_INSTANCES,
                ) - value
            keyStore.save(key = CUSTOM_INSTANCES_KEY, value = instances)
        }
}
