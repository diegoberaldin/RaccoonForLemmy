package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore

private const val CUSTOM_INSTANCES_KEY = "customInstances"
private val DEFAULT_INSTANCES = listOf(
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
    override suspend fun getAll(): List<String> {
        if (!keyStore.containsKey(CUSTOM_INSTANCES_KEY)) {
            keyStore.save(key = CUSTOM_INSTANCES_KEY, value = DEFAULT_INSTANCES)
        }
        return keyStore.get(key = CUSTOM_INSTANCES_KEY, default = DEFAULT_INSTANCES)
    }

    override suspend fun add(value: String) {
        val instances = keyStore.get(
            key = CUSTOM_INSTANCES_KEY,
            default = DEFAULT_INSTANCES,
        ).toSet() + value
        keyStore.save(key = CUSTOM_INSTANCES_KEY, value = instances.toList().sorted())

    }

    override suspend fun remove(value: String) {
        val instances = keyStore.get(
            key = CUSTOM_INSTANCES_KEY,
            default = DEFAULT_INSTANCES,
        ) - value
        keyStore.save(key = CUSTOM_INSTANCES_KEY, value = instances)
    }
}
