package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

private const val SETTINGS_KEY = "communitySort"

internal class DefaultCommunitySortRepository(
    private val keyStore: TemporaryKeyStore,
) : CommunitySortRepository {
    override suspend fun get(handle: String): Int? = withContext(Dispatchers.IO) {
        val map = deserializeMap()
        map[handle]
    }

    override suspend fun save(
        handle: String,
        value: Int,
    ) = withContext(Dispatchers.IO) {
        val map = deserializeMap()
        map[handle] = value
        val newValue = serializeMap(map)
        keyStore.save(SETTINGS_KEY, newValue)
    }

    private fun deserializeMap(): MutableMap<String, Int> =
        keyStore.get(SETTINGS_KEY, listOf()).mapNotNull {
            it.split(":").takeIf { e -> e.size == 2 }?.let { e -> e[0] to e[1] }
        }.let { pairs ->
            val res = mutableMapOf<String, Int>()
            for (pair in pairs) {
                res[pair.first] = pair.second.toInt()
            }
            res
        }

    private fun serializeMap(map: Map<String, Int>): List<String> =
        map.map { e ->
            e.key + ":" + e.value
        }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        keyStore.remove(SETTINGS_KEY)
    }
}
